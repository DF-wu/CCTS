package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.entity.CCTSModel.CCTSDocument;
import tw.dfder.ccts.entity.CCTSModel.NextState;
import tw.dfder.ccts.entity.Contract;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.EventLog;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.EventLogRepository;
import tw.dfder.ccts.services.pact_broker.PactBrokerBusyBox;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

@Service("CCTSVerifier")
public class CCTSVerifier {
    private final CCTSDocumentParser documentParser;
    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;
    private final PactBrokerBusyBox busyBox;

    @Autowired
    public CCTSVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository, PactBrokerBusyBox busyBox) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
        this.busyBox = busyBox;
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
            isSuccess = errorTeller(errorOccursMap);
            if(isSuccess){
                // this CCTS document pass
            }else {
                // error happend
                isSuccess = false;
            }
        }
        return isSuccess;
    }

    private boolean errorTeller(Map<NextState, CCTSStatusCode> errors) {
        if(errors.size() > 0) {
//            error occurred
            System.out.println("error occurred!!!");
            System.out.println("list below:");


//            for (CCTSStatusCode err : errors)  {
//                System.out.println(err.getInfoMessage());
//            }
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




}

