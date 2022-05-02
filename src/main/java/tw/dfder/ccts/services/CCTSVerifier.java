package tw.dfder.ccts.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.Contract;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.EventLog;
import tw.dfder.ccts.entity.cctsdocumentmodel.SimpleState;
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



    public CCTSResult verifyCCTSDelivery(CCTSResult cctsResult) {
        // retrieve needed data from db to memory for increasing speed
        ArrayList<CCTSDocument> documents = (ArrayList<CCTSDocument>) cctsDocumentRepository.findAll();
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();

        for (CCTSDocument document : documents) {

//            HashSet<NextState> reachableStatesSet = flatenPaths(paths);

            // get all possible paths
            ArrayList<ArrayList<NextState>> paths = new ArrayList<ArrayList<NextState>>();
            documentParser.pathFinder(
                    document,
                    document.findSimpleState(document.getStartAt()),
                    new ArrayList<>(),
                    paths);

            // verify timeSequenceLabel
            for (ArrayList<NextState> path : paths) {
                for (int i = 1; i < path.size(); i++) {
                    // timeSequenceLabel always be increased
                    if (path.get(i).getTimeSequenceLabel() > path.get(i - 1).getTimeSequenceLabel()) {


                    }

                }
            }







        }





//        --------------------------old flow------------------------
        for (CCTSDocument document : documents) {
            // specify a delivery as baseline.
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
                if (sameRouteEventlogs.size() == 0) {
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


            // traversal all potential valid path.
            ArrayList<ArrayList<NextState>> traversalPaths = new ArrayList<>();
            ArrayList<NextState> processingList = new ArrayList<>();
            // find initial simple state
            SimpleState initialState = null;
            for (SimpleState state : document.getStates()){
                if(state.getStateName().equals(document.getStartAt())) {
                    initialState = state;
                }
            }
            // start from initial state
            documentParser.pathFinder(document, initialState, processingList, traversalPaths);


            // check if all paths are valid
            for (ArrayList<NextState> path : traversalPaths) {
                // check if path is valid
                cctsResult.getPathVerificationResults().put(
                        toPathString(path),
                        verifyPathResult(path, eventlogs));
            }


        }

        // gererate final result
        cctsResult.checkOut();
        cctsResultRepository.save(cctsResult);
        return cctsResult;
    }

    private HashSet<NextState> flatenPaths(ArrayList<ArrayList<NextState>> paths){
        HashSet<NextState> pathSet = new HashSet<>();
        for (ArrayList<NextState> path : paths){
            for ( NextState state : path){
                pathSet.add(state);
            }
        }
        return pathSet;
    }

    // To store as key
    private String toPathString(ArrayList<NextState> path) {
        String s = "";
        for (NextState nextState : path) {
            s += nextState.getStateName() + " -> ";
        }

        // remove last " -> "
        s = s.substring(0, s.length() - 4);
        return s;
    }


    private CCTSStatusCode verifyPathResult(ArrayList<NextState> path, ArrayList<EventLog> eventlogs) {
        for (int i = 1; i < path.size(); i++) {
            // verify time sequence label
            // current state timeSequenceLabel shuold be lager than previous state timeSequenceLabel
            if (!(path.get(i).getTimeSequenceLabel() > path.get(i - 1).getTimeSequenceLabel())) {
                // if not, return error code
                return CCTSStatusCode.PATH_SEQUENCE_ERROR;
            }
            // verify time consumer provider logically correct.
            if (!(path.get(i).getProvider().equals(path.get(i - 1).getConsumer()))) {
                // if not, return error code
                return CCTSStatusCode.PATH_NOT_CONNECTED;
            }

//            TODO: decide the verify process is path base or document with eventlog base
//            // verify eventlog time sequence and consumer provider follow the path
//            for (NextState ns : path) {
//                // find eventlog with same time sequence and consumer provider
//                boolean flag=false;
//                for (EventLog eventlog : eventlogs) {
//                    if (eventlog.getTimeSequenceLabel().equals(ns.getTimeSequenceLabel())
//                            && eventlog.getConsumerName().equals(ns.getConsumer())
//                            && eventlog.getProviderName().equals(ns.getProvider())) {
//                        // if found, go next round
//                        // any one found == valid path
//                        flag=true;
//                        break;
//                    }
//                }
//                if(!flag){
//                    // no eventlog found for this path, return error code
//                    return CCTSStatusCode.PATH_NOT_EVENTLOG_FOUND;
//                }
//            }





        }
        // all pass
        return CCTSStatusCode.ALLGREEN;
    }

        private CCTSStatusCode verifyEventlogSequence (ArrayList < EventLog > eventlogs, CCTSDocument
        document, ArrayList < Integer > caseSequence){
            ArrayList<NextState> referenceNextStates = findCaseSequenceNextStates(caseSequence, document);
            ArrayList<EventLog> caseSequenceEventLogs = new ArrayList<>();
            for (NextState nextState : referenceNextStates) {
                ArrayList<EventLog> sameNextStateEventLogs = new ArrayList<>();
                for (EventLog eventLog : eventlogs) {
                    // may be more than one eventlogs for one nextState
                    if (nextState.getConsumer().equals(eventLog.getConsumerName())
                            && nextState.getProvider().equals(eventLog.getProviderName())
                            && nextState.getTestCaseId().equals(eventLog.getTestCaseId())
                            && nextState.getTimeSequenceLabel().equals(eventLog.getTimeSequenceLabel())) {
                        sameNextStateEventLogs.add(eventLog);
                    }
                }

                // may have multiple eventlogs qualified the nextstate, but we only need one to prove the delivery is valid
                EventLog earliestEventLog = new EventLog(Long.MAX_VALUE, "", "", "", 0);
                for (EventLog el : sameNextStateEventLogs) {
                    // collect and find the earliest eventlog as the reference eventlog
                    if (el.getTimeStamp() <= earliestEventLog.getTimeStamp()) {
                        earliestEventLog = el;
                    }
                }
                caseSequenceEventLogs.add(earliestEventLog);
            }

            // verify sequence of time to fit the case sequence
            for (int i = 1; i < caseSequenceEventLogs.size(); i++) {
                if (caseSequenceEventLogs.get(i).getTimeStamp() > caseSequenceEventLogs.get(i - 1).getTimeStamp()) {
                    // right sequence
                } else {
                    // wrong sequence
                    return CCTSStatusCode.PATH_SEQUENCE_ERROR;
                }
            }

            // verify each nextState provider should be connected by previous nextState consumer
            for (int i = 1; i < referenceNextStates.size(); i++) {
                if (referenceNextStates.get(i).getProvider().equals(referenceNextStates.get(i - 1).getConsumer())) {
                    // provider and consumer are the same,  the previous nextState provider is the same as current nextState consumer
                } else {
                    // provider and consumer are not the same,  return error code
                    return CCTSStatusCode.PATH_NOT_CONNECTED;
                }
            }

            return CCTSStatusCode.ALLGREEN;
        }

        private ArrayList<NextState> findCaseSequenceNextStates (ArrayList < Integer > caseSequence, CCTSDocument
        document){
            ArrayList<NextState> nextStateSequence = new ArrayList<>();
            for (Integer integer : caseSequence) {
                for (NextState nextState : documentParser.findDeliveryList(document)) {
                    if (nextState.getTimeSequenceLabel().equals(integer)) {
                        nextStateSequence.add(nextState);
                    }
                }
            }
            return nextStateSequence;
        }


        private CCTSResultRecord verifyDeliveryAndEventlog (NextState
        delivery, ArrayList < EventLog > sameRouteEventlogs, String documentName){
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
                if (record.getErrorCode() == CCTSStatusCode.ALLGREEN) {
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
        private boolean errorTellerContractTestError (Map < String, CCTSStatusCode > errors){
            if (errors.size() > 0) {
                //error occurred
                System.out.println("Not Pass Contract Test Services:");
                for (String service : errors.keySet()) {
                    System.out.println("  Service: " + service);
                    System.out.println("  Error Message: " + errors.get(service).getMessage());
                }
                return false;
            } else {
                // pass
                return true;
            }
        }

        private boolean errorTellerPathMathError (Map < NextState, CCTSStatusCode > errors){
            if (errors.size() > 0) {
//            error occurred
                System.out.println("Error occurred!!!");
                System.out.println("List below:");
                for (NextState state : errors.keySet()) {
                    System.out.println("Error message: " + errors.get(state).getMessage());
                    System.out.println(state.toPrretyString());
                }
                return false;
            } else {
                // all green~~
                return true;
            }
        }

        private CCTSStatusCode verifyDeliveryAndContract (NextState delivery){
            // pull contract with delivery's provider & consumer
            Contract contract = busyBox.getContractFromBroker(
                    delivery.getProvider(),
                    delivery.getConsumer()
            );
            boolean isValidDelivery = false;
            for (String id : contract.getTestCaseIds()) {
                if (delivery.getTestCaseId().equals(id)) {
                    // found
                    isValidDelivery = true;
                } else {
                    // not this one
                }
            }
            // result
            return isValidDelivery ? CCTSStatusCode.ALLGREEN : CCTSStatusCode.DELIVERY_TESTCASEID_NOT_FOUND_IN_CONTRACT;
        }

        private CCTSStatusCode inspectDeliveryAndEventLog (NextState delivery, EventLog el){
            if (delivery.getProvider().equals(el.getProviderName())) {
                // provider match
                if (delivery.getConsumer().equals(el.getConsumerName())) {
                    // consumer match
                    if (delivery.getTestCaseId().equals(el.getTestCaseId())) {
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

        private Map<String, CCTSStatusCode> validateServiceContractTestResult (CCTSDocument document){
            // get all participants
            HashSet<String> participantsServices = new HashSet<String>();
            for (NextState state : documentParser.findDeliveryList(document)) {
                participantsServices.add(state.getProvider());
                participantsServices.add(state.getConsumer());
            }

            // create an results Dict
            HashMap<String, CCTSStatusCode> results = new HashMap<String, CCTSStatusCode>();
            HashMap<String, Boolean> contractTestresults = fetchContractTestResultConcurrently(participantsServices);
            for (String service : contractTestresults.keySet()) {
                if (contractTestresults.get(service)) {
                    //true = pass
                    results.put(service, CCTSStatusCode.ALLGREEN);
                } else {
                    // false = error
                    results.put(service, CCTSStatusCode.CONTREACT_TEST_RESULT_NOT_PASS);
                }
            }

            return results;
        }

        private Boolean pactCLIProcessInvocker (String participant){
            // use docker tool
            String commandTemplate = "docker run --rm  pactfoundation/pact-cli:latest broker can-i-deploy --pacticipant %s --latest --broker-base-url %s";
            try {
                // create a process to excute can-i-deploy tool
                Process process;
                process = Runtime.getRuntime().exec(String.format(commandTemplate, participant, serviceConfig.pactBrokerUrl));

                // get tool's std out
                StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
                Executors.newSingleThreadExecutor().submit(streamGobbler);

                // retrieve result. 0 for yes, 1 for no
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    // pass
                    return true;
                } else {
                    // some contracts test not pass
                    return false;
                }

            } catch (Exception e) {
                System.out.println("Fetch result of can-i-deploy fail!");
                System.out.println("Service name: " + participant);
                e.printStackTrace();
                return false;
            }
        }

        private HashMap<String, Boolean> fetchContractTestResultConcurrently (HashSet < String > participants) {
            ArrayList<Thread> threadPool = new ArrayList<>();
            HashMap<String, Boolean> resultMap = new HashMap<String, Boolean>();
            // define job
            for (String participant : participants) {
                threadPool.add(new Thread(() -> {
                    resultMap.put(participant, pactCLIProcessInvocker(participant));
                }));
            }
            //start
            for (Thread t : threadPool) {
                t.start();
            }

            // join threads
            try {
                for (Thread t : threadPool) {
                    t.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return resultMap;

        }

    }

