package tw.dfder.ccts.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.Contract;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.EventLog;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResultRecord;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.CCTSResultRepository;
import tw.dfder.ccts.repository.EventLogRepository;
import tw.dfder.ccts.services.pact_broker.PactBrokerBusyBox;

import java.util.*;
import java.util.concurrent.Executors;

@Service("CCTSVerifier")
public class CCTSVerifier {
    private final CCTSDocumentParser documentParser;
    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;
    private final CCTSResultRepository cctsResultRepository;
    private final PactBrokerBusyBox busyBox;
    private final ServiceConfigure serviceConfig;
    @Autowired
    public CCTSVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository, CCTSResultRepository cctsResultRepository, PactBrokerBusyBox busyBox, ServiceConfigure serviceConfig) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
        this.cctsResultRepository = cctsResultRepository;
        this.busyBox = busyBox;
        this.serviceConfig = serviceConfig;
    }

    public CCTSResult verifyCCTSProfileSAGAFlow() {
        // retrieve needed data from db to memory for increasing speed
        ArrayList<CCTSDocument> documents = (ArrayList<CCTSDocument>) cctsDocumentRepository.findAll();
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();
        Hashtable<NextState, CCTSStatusCode> resultDict = new Hashtable<>();

        CCTSResult cctsResult  = new CCTSResult(documents);

        for (CCTSDocument document : documents) {
            // specify a path as baseline.
            for (NextState delivery : documentParser.findDeliveryList(document)) {
                //extract same provider and consumer eventlog
                ArrayList<EventLog> sameRouteEventlogs = new ArrayList<>();
                for (EventLog el : eventlogs) {
                    // same provider consumer
                    if (delivery.getConsumer().equals(el.getConsumerName()) && delivery.getProvider().equals(el.getProviderName())) {
                        sameRouteEventlogs.add(el);
                    }
                }
                // if same route eventlogs not exist -> no related event was produced between the producer and consumer
                if(sameRouteEventlogs.size() == 0 ){
                    cctsResult.getResultBetweenDeliveryAndEventLogs().add(
                            new CCTSResultRecord(document.getTitle(), delivery, CCTSStatusCode.ERROR_NO_EVENT_FOUND));
                    // jump to next delivery
                    continue;
                }

                // verify delivery and eventlogs and get error code if exist
                // add errors to result
                cctsResult.getResultBetweenDeliveryAndEventLogs().add(
                        verifyDeliveryAndEventlog(delivery, sameRouteEventlogs, document.getTitle()));

                // match delivery and corresponded contract testCaseId witch is from pact broker
                cctsResult.getResultBetweenDeliveryAndContract().add(
                        new CCTSResultRecord(document.getTitle(), delivery, verifyDeliveryAndContract(delivery)));
            }

            // check contract verification status
            cctsResult.getContractVerificationResults().putAll(validateServiceContractTestResult(document));

        }
        cctsResult.checkOut();
        cctsResultRepository.save(cctsResult);
        return cctsResult;
    }

    private CCTSResultRecord verifyDeliveryAndEventlog(NextState delivery, ArrayList<EventLog> sameRouteEventlogs, String documentName) {
        ArrayList<CCTSResultRecord> results = new ArrayList<>();
        // match delivery and eventlog
        CCTSStatusCode inspectResult = null;

        for (EventLog el : sameRouteEventlogs) {
            // add every status of delivery eventlog pair in to array.

            inspectResult = inspectDeliveryAndEventLog(delivery, el);
            results.add(new CCTSResultRecord(documentName, delivery, inspectResult));
        }


        // check result array
        for (CCTSResultRecord record : results) {
            if(record.getErrorCode() == CCTSStatusCode.ALLGREEN){
                // there is a valid eventlog for this delivery
                return record;
            }
        }
        // if no one valid eventlog found.
        return new CCTSResultRecord(documentName, delivery, CCTSStatusCode.ERROR_NO_MATCH_TESTCASEID_IN_EVENTLOGS);





        // if error ->  add into errors,
        // if no error -> passed
//
//
//        if(inspectResult == CCTSStatusCode.ALLGREEN){
//            // found correspond evnetlog with this delivery
//            isValidDelivery = true;
//        }else {
//            // not this eventlog
//            isValidDelivery = false;
//        }
        // eventlog and delivery not match. add into errors map.

        // for this delivery only. if error occur, add once and leave loop.
    }


    //for inspect contract test result error
    private boolean errorTellerContractTestError(Map<String, CCTSStatusCode> errors){
        if (errors.size() > 0){
            //error occurred
            System.out.println("Not Pass Contract Test Services:");
            for (String service :errors.keySet()) {
                System.out.println("  Service: " + service);
                System.out.println("  Error Message: " + errors.get(service).getMessage());
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
                System.out.println("Error message: " + errors.get(state).getMessage());
                System.out.println(state.toPrretyString());
            }
            return false;
        }else
        {
            // all green~~
            return true;
        }
    }

    private CCTSStatusCode verifyDeliveryAndContract(NextState delivery) {
        // pull contract with delivery's provider & consumer
        Contract contract = busyBox.getContractFromBroker(
                delivery.getProvider(),
                delivery.getConsumer()
        );
        boolean isValidDelivery = false;
        for (String id : contract.getTestCaseIds()) {
            if(delivery.getTestCaseId().equals(id)){
                // found
                isValidDelivery = true;
            }else {
                // not this one
            }
        }
        // result
        return isValidDelivery ? CCTSStatusCode.ALLGREEN : CCTSStatusCode.DELIVERY_TESTCASEID_NOT_FOUND_IN_CONTRACT;
    }

    private CCTSStatusCode inspectDeliveryAndEventLog(NextState delivery, EventLog el){
        if (delivery.getProvider().equals(el.getProviderName())) {
            // provider match
            if (delivery.getConsumer().equals(el.getConsumerName())) {
                // consumer match
                if (delivery.getTestCaseId().equals(el.gettestCaseId())) {
                    // testCaseId match
                    // all condition match -> delivery passed
                    return CCTSStatusCode.ALLGREEN;
                } else {
                    // testsCaseId not Match
                    return CCTSStatusCode.ERROR_TESTCASEID_IN_CONTRACT;
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
        for (NextState state: documentParser.findDeliveryList(document)) {
            participantsServices.add(state.getProvider());
            participantsServices.add(state.getConsumer());
        }

        // create an results Dict
        HashMap<String,  CCTSStatusCode> results = new HashMap<String, CCTSStatusCode>();
        HashMap<String, Boolean> contractTestresults = fetchContractTestResultConcurrently(participantsServices);
        for (String service: contractTestresults.keySet()) {
            if(contractTestresults.get(service)){
                //true = pass
                results.put(service, CCTSStatusCode.ALLGREEN);
            }else{
                // false = error
                results.put(service, CCTSStatusCode.CONTREACT_TEST_RESULT_NOT_PASS);
            }
        }

        return results;
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

