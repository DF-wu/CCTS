package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.CCTSModel.CCTSDocument;
import tw.dfder.ccts.entity.CCTSModel.NextState;
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

    public boolean verifyCCTSProfileSAGAFlow() {
        boolean isSuccess = false;

        // retrieve needed data from db to memory for increasing speed
        ArrayList<CCTSDocument> documents = (ArrayList<CCTSDocument>) cctsDocumentRepository.findAll();
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();
        Hashtable<NextState, CCTSStatusCode> errorOccursMap = new Hashtable<>();

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

                // match path and eventlog
                for (EventLog el : sameRouteEventlogs) {
                    // if error ->  add into errors,
                    // if no error -> passed
                     CCTSStatusCode pathAndElResult = inspectErrors(path, el);
                    if(pathAndElResult == CCTSStatusCode.ALLGREEN){
                        // passed
                    }else {
                        // eventlog and path not match. add into errors map.
                        errorOccursMap.put(path, inspectErrors(path, el));
                    }
                }

                // match path and contract which is from pact broker
                CCTSStatusCode pathAndContractResult = verifyPathAndContract(path);
                if(pathAndContractResult == CCTSStatusCode.ALLGREEN){
                    // passed
                }else {
                    // add into error map
                    errorOccursMap.put(path, pathAndContractResult);
                }
            }


            // check contract test status
            isSuccess =  errorTellerContractTestError(validateServiceContractTestResult(document));


            // this document's error
            isSuccess = errorTellerPathMathError(errorOccursMap);


        }
        return isSuccess;
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
        return !isValidPath ? CCTSStatusCode.PATH_TESTCASE_NOT_FOUND_IN_CONTRACT : CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode inspectErrors(NextState path, EventLog el){
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
                return CCTSStatusCode.ERROR_CONSUMER;
            }
        } else {
            // provider not match
            return CCTSStatusCode.ERROR_PROVIDER;
        }

    }

    private Map<String, CCTSStatusCode> validateServiceContractTestResult(CCTSDocument document){
        // get all participants
        HashSet<String> participantsServices = new HashSet<String>();
        for (NextState state: documentParser.findPathList(document)) {
            participantsServices.add(state.getProvider());
            participantsServices.add(state.getConsumer());
        }

        HashMap<String,  CCTSStatusCode> errors = new HashMap<String, CCTSStatusCode>();
        for (String service: participantsServices) {
            if(pactCLIProcessInvocker(service)) {
                //pass
            }else {
                // fail
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
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
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
                return false;
            }
    }



}

