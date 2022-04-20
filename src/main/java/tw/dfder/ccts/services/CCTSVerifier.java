package tw.dfder.ccts.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.Contract;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.EventLog;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.EventLogRepository;
import tw.dfder.ccts.services.pact_broker.PactBrokerBusyBox;

import java.util.*;
import java.util.concurrent.Executors;

@Service("CCTSVerifier")
public class CCTSVerifier {
    private final CCTSDocumentParser documentParser;
    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;
    private final PactBrokerBusyBox busyBox;
    private final ServiceConfigure serviceConfig;
    @Autowired
    public CCTSVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository, PactBrokerBusyBox busyBox, ServiceConfigure serviceConfig) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
        this.busyBox = busyBox;
        this.serviceConfig = serviceConfig;
    }

    public CCTSResult verifyCCTSProfileSAGAFlow() {

        boolean isSuccess = false;

        // retrieve needed data from db to memory for increasing speed
        ArrayList<CCTSDocument> documents = (ArrayList<CCTSDocument>) cctsDocumentRepository.findAll();
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();
        Hashtable<NextState, CCTSStatusCode> resultDict = new Hashtable<>();

        CCTSResult cctsResult  = new CCTSResult(documents);

        for (CCTSDocument document : documents) {
            // specify a path as baseline.
            for (NextState path : documentParser.findPathList(document)) {
                //extract same provider and consumer eventlog
                ArrayList<EventLog> sameRouteEventlogs = new ArrayList<>();
                for (EventLog el : eventlogs) {
                    // same provider consumer
                    if (path.getConsumer().equals(el.getConsumerName()) && path.getProvider().equals(el.getProviderName())) {
                        sameRouteEventlogs.add(el);
                    }
                }

                // if same route eventlogs not exist -> no related event was produced between the producer and consumer
                if(sameRouteEventlogs.size() == 0 ){
                    cctsResult.getResultBetweenPathAndEventLogs().put(path, CCTSStatusCode.ERROR_NO_EVENT_FOUND);
                    // jump to next path
                    continue;
                }

                // verify path and eventlogs and get error code if exist
                // add errors to result
                cctsResult.getResultBetweenPathAndEventLogs().putAll(verifyPathAndEventlog(path, sameRouteEventlogs));

                // match path and corresponded contract testCaseId
                cctsResult.getResultBetweenPathAndContract().put(path, verifyPathAndContract(path));
            }

            // check contract verification status
            cctsResult.getContractVerificationErrors().putAll(validateServiceContractTestResult(document));

        }
        return cctsResult;
    }

    private Hashtable<NextState, CCTSStatusCode> verifyPathAndEventlog (NextState path, ArrayList<EventLog> sameRouteEventlogs) {
        Hashtable<NextState, CCTSStatusCode> errorsDict = new Hashtable<>();
        // match path and eventlog
        for (EventLog el : sameRouteEventlogs) {
            // if error ->  add into errors,
            // if no error -> passed
             CCTSStatusCode pathAndElResult = inspectPathAndEventLog(path, el);
            if(pathAndElResult == CCTSStatusCode.ALLGREEN){
                // pass
            }else {
                // eventlog and path not match. add into errors map.
                errorsDict.put(path, pathAndElResult);
            }
        }
        return errorsDict;
    }


    //for inspect contract test result error
    private boolean errorTellerContractTestError(Map<String, CCTSStatusCode> errors){
        if (errors.size() > 0){
            //error occurred
            System.out.println("Not Pass Contract Test Services:");
            for (String service :errors.keySet()) {
                System.out.println("  Service: " + service);
                System.out.println("  Error Message: " + errors.get(service).getInfoMessage());
            }
            return false;
        }else{
            // pass
            return true;
        }
    }

    private boolean errorTellerPathMathError(Map<NextState, CCTSStatusCode> errors) {
        if(errors.size() > 0) {
//            error occurred
            System.out.println("Error occurred!!!");
            System.out.println("List below:");
            for (NextState state  : errors.keySet()){
                System.out.println("Error message: " + errors.get(state).getInfoMessage());
                System.out.println(state.toPrretyString());
            }
            return false;
        }else
        {
            // all green~~
            return true;
        }
    }

    private CCTSStatusCode verifyPathAndContract(NextState path) {
        // pull contract with path's provider & consumer
        Contract contract = busyBox.getContractFromBroker(
                path.getProvider(),
                path.getConsumer()
        );
        boolean isValidPath = false;
        for (String id : contract.getTestCaseIds()) {
            if(path.getTestCaseId().equals(id)){
                // found
                isValidPath = true;
            }else {
                // not this one
            }
        }
        // result
        return isValidPath ? CCTSStatusCode.ALLGREEN : CCTSStatusCode.PATH_TESTCASE_NOT_FOUND_IN_CONTRACT;
    }

    private CCTSStatusCode inspectPathAndEventLog(NextState path, EventLog el){
        if (path.getProvider().equals(el.getProviderName())) {
            // provider match
            if (path.getConsumer().equals(el.getConsumerName())) {
                // consumer match
                if (path.getTestCaseId().equals(el.gettestCaseId())) {
                    // testCaseId match
                    // all condition match -> path passed
                    return CCTSStatusCode.ALLGREEN;
                } else {
                    // testsCaseId not Match
                    return CCTSStatusCode.ERROR_TESTCASEID;
                }
            } else {
                //consumer not match
                return CCTSStatusCode.ERROR_PARTICIPANT;
            }
        } else {
            // provider not match
            return CCTSStatusCode.ERROR_PARTICIPANT;
        }

    }

    private Map<String, CCTSStatusCode> validateServiceContractTestResult(CCTSDocument document){
        // get all participants
        HashSet<String> participantsServices = new HashSet<String>();
        for (NextState state: documentParser.findPathList(document)) {
            participantsServices.add(state.getProvider());
            participantsServices.add(state.getConsumer());
        }

        // create an errors Dict
        HashMap<String,  CCTSStatusCode> errors = new HashMap<String, CCTSStatusCode>();
        HashMap<String, Boolean> contractTestresults = fetchContractTestResultConcurrently(participantsServices);
        for (String service: contractTestresults.keySet()) {
            if(contractTestresults.get(service)){
                //true = pass
            }else{
                // false = error
                errors.put(service, CCTSStatusCode.CONTREACT_TEST_RESULT_NOT_PASS);
            }
        }

        return errors;
    }

    private Boolean pactCLIProcessInvocker(String participant){
        // use docker tool
        String commandTemplate = "docker run --rm  pactfoundation/pact-cli:latest broker can-i-deploy --pacticipant %s --latest --broker-base-url %s";
        try{
            // create a process to excute can-i-deploy tool
            Process process;
            process = Runtime.getRuntime().exec(String.format(commandTemplate, participant, serviceConfig.pactBrokerUrl));

            // get tool's std out
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);

            // retrieve result. 0 for yes, 1 for no
            int exitCode = process.waitFor();

            if(exitCode == 0){
                // pass
                return true;
            }else{
                // some contracts test not pass
                return false;
            }

        }catch (Exception e) {
                System.out.println("Fetch result of can-i-deploy fail!");
                System.out.println("Service name: " + participant);
                e.printStackTrace();
                return false;
            }
    }

    private HashMap<String, Boolean> fetchContractTestResultConcurrently(HashSet<String> participants) {
        ArrayList<Thread> threadPool = new ArrayList<>();
        HashMap<String, Boolean> resultMap = new HashMap<String, Boolean>();
        // define job
        for (String participant : participants) {
            threadPool.add(new Thread( () -> {
                resultMap.put(participant,pactCLIProcessInvocker(participant));
            }));
        }
        //start
        for (Thread t :threadPool) {
            t.start();
        }

        // join threads
        try {
            for (Thread t :threadPool) {
                t.join();
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        return resultMap;

    }

}

