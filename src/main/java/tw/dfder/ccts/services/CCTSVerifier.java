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
import tw.dfder.ccts.repository.CCTSTestRepository;
import tw.dfder.ccts.repository.EventLogRepository;
import tw.dfder.ccts.services.pact_broker.PactBrokerBusyBox;

import java.util.*;
import java.util.concurrent.Executors;

@Service("CCTSVerifier")
public class CCTSVerifier {
    private final CCTSDocumentParser documentParser;
    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;
    private final CCTSTestRepository cctsTestRepository;
    private final PactBrokerBusyBox busyBox;
    private final ServiceConfigure serviceConfig;

    @Autowired
    public CCTSVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository, CCTSTestRepository cctsTestRepository, PactBrokerBusyBox busyBox, ServiceConfigure serviceConfig) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
        this.cctsTestRepository = cctsTestRepository;
        this.busyBox = busyBox;
        this.serviceConfig = serviceConfig;
    }



    public CCTSResult verifyCCTSDelivery(CCTSResult cctsResult) {
        CCTSDocument cctsDocument = cctsResult.getDocument();
        // retrieve needed data from db to memory for increasing speed
        ArrayList<CCTSDocument> documents = (ArrayList<CCTSDocument>) cctsDocumentRepository.findAll();
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();


        // get all possible paths
        ArrayList<ArrayList<NextState>> paths = new ArrayList<ArrayList<NextState>>();
        documentParser.pathFinder(
                    cctsDocument,
                    cctsDocument.findSimpleState(cctsDocument.getStartAt()),
                    new ArrayList<>(),
                    paths);

        // eventlog stage
        // check all correspond eventlogs are valid
        // check testCaseId
        HashSet<NextState> reachableStatesSet = flatenPaths(paths);
        for (NextState delivery : reachableStatesSet) {
            ArrayList<EventLog> sameRouteEventlogs = findSameRouteEventlogs(delivery.getProvider(), delivery.getConsumer(), eventlogs);
            if(sameRouteEventlogs.size() == 0) {
                // fail because no eventlogs found
                cctsResult.getResultBetweenDeliveryAndEventLogs().add(
                        new CCTSResultRecord(cctsResult.getDocument().getTitle(),
                                delivery,
                                CCTSStatusCode.ERROR_NO_EVENT_FOUND));
            }else{
                boolean isValid = false;
                for (EventLog eventLog : sameRouteEventlogs) {
                    if(eventLog.getTestCaseId().equals(delivery.getTestCaseId())) {
                        isValid = true;
                    }
                }
                if(isValid) {
                    //evenlog found
                    cctsResult.getResultBetweenDeliveryAndEventLogs().add(
                            new CCTSResultRecord(
                                    cctsResult.getDocument().getTitle(),
                                    delivery,
                                    CCTSStatusCode.ALLGREEN));
                }else{
                    // fail because no matched testcaseId eventlogs found
                    cctsResult.getResultBetweenDeliveryAndEventLogs().add(
                            new CCTSResultRecord(
                                    cctsResult.getDocument().getTitle(),
                                    delivery,
                                    CCTSStatusCode.ERROR_NO_MATCH_TESTCASEID_IN_EVENTLOGS));
                }
            }
        }

        // path verify stage
        // find at least a valid path through all eventlogs by path
        for ( ArrayList<NextState> path : paths) {
            cctsResult.getPathVerificationResults().put(CCTSDocumentParser.path2StringName(path), findValidEventlogComposition(path, eventlogs, cctsResult));
        }

        // contract stage
        for (NextState delivery : reachableStatesSet) {
            cctsResult.getResultBetweenDeliveryAndContract().add(
                    new CCTSResultRecord(cctsResult.getDocument().getTitle(), delivery, verifyDeliveryAndContract(delivery)));
        }

        // contract test stage
        cctsResult.getContractVerificationResults().putAll(validateServiceContractTestResult(cctsDocument));




        // gererate final result
        cctsResult.checkOut();
        return cctsResult;
    }

    private CCTSStatusCode findValidEventlogComposition(ArrayList<NextState> path, ArrayList<EventLog> eventlogs, CCTSResult cctsResult) {
        EventLog pilovtEventlog = new EventLog(0, "", "","",0);

        for (NextState delivery : path) {
            // extract same provider and consumer eventlog
            ArrayList<EventLog> sameRouteEventlogs = findSameRouteEventlogs(delivery.getProvider() , delivery.getConsumer(), eventlogs);
            // Sort eventlogs by timestamp by ascending order
            sameRouteEventlogs.sort((o1, o2) -> {
                if(o1.getTimeStamp()<o2.getTimeStamp()) {
                    return -1;
                }else{
                    return 1;
                }
            });

//    no need now because we have already done it in the previous step
//            // check array if empty -> no eventlogs found
//            if(sameRouteEventlogs.size() == 0) {
//                cctsResult.getResultBetweenDeliveryAndEventLogs().add(
//                        new CCTSResultRecord(cctsResult.getDocument().getTitle(), delivery, CCTSStatusCode.ERROR_NO_EVENT_FOUND));
//                return CCTSStatusCode.ERROR_NO_EVENT_FOUND;
//            }

            // find eventlogs
            boolean isDeliveryValid = false;
            for (EventLog eventlog : sameRouteEventlogs) {
                 if(eventlog.getTimeStamp() > pilovtEventlog.getTimeStamp()) {
                     // found eventlog
                     pilovtEventlog = eventlog;
                     isDeliveryValid = true;
                     break;
                 }
            }

            if(!isDeliveryValid) {
                return CCTSStatusCode.NOT_AT_LEAST_A_VALID_EVENTLOG_COMPOSITION_PATH_FOUND;
            }


        }
        return CCTSStatusCode.ALLGREEN;
    }

    private ArrayList<EventLog> findSameRouteEventlogs(String provider, String consumer, ArrayList<EventLog> eventlogs) {
        ArrayList<EventLog> sameRouteEventlogs = new ArrayList<>();
        for (EventLog el : eventlogs) {
            if (el.getConsumerName().equals(consumer) && el.getProviderName().equals(provider)) {
                sameRouteEventlogs.add(el);
            }
        }
        return sameRouteEventlogs;
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




    private CCTSStatusCode verifyPathResult(ArrayList<NextState> path, ArrayList<EventLog> eventlogs) {
        for (int i = 1; i < path.size(); i++) {
            // verify time sequence label
            // current state timeSequenceLabel shuold be lager than previous state timeSequenceLabel
            if (!(path.get(i).getTimeSequenceLabel() > path.get(i - 1).getTimeSequenceLabel())) {
                // if not, return error code
                return CCTSStatusCode.PATH_TIMESEQUENCELABEL_NOT_INCREASED;
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
                    return CCTSStatusCode.NOT_AT_LEAST_A_VALID_EVENTLOG_COMPOSITION_PATH_FOUND;
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

                // pass
                // some contracts test not pass
                return exitCode == 0;

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

