package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.cctsdocumentmodel.SimpleState;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSDocumentError;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.EventLogRepository;

import java.util.ArrayList;
import java.util.HashSet;

import static tw.dfder.ccts.entity.CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;

@Service("DocumentVerifier")
public class DocumentVerifier {
    public String currentDocumentTitle = "";
    private final CCTSDocumentParser documentParser;

    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;




    @Autowired
    public DocumentVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
    }

    public CCTSStatusCode prepareDocumentVerify(CCTSTest cctsTest) {
        CCTSStatusCode parseDocumentCode =  documentParser.parseAllCCTSProfilesAndSave2DB(cctsTest);
        if (parseDocumentCode != CCTSStatusCode.ALLGREEN) {
            return parseDocumentCode;
        }

        // if parse all documents successfully, retrieve all documents from DB
        cctsTest.addDocuments((ArrayList<CCTSDocument>) cctsDocumentRepository.findAll());

        //verify all documents have unique title
        CCTSStatusCode documentVerifiedResult = documentTitleVerifier(cctsTest);
        if (documentVerifiedResult != CCTSStatusCode.ALLGREEN) {
            cctsTest.setDuplicatedTitle(true);
            return CCTSStatusCode.CCTSDOCUMENT_DUPLICATED_TITLE_ERROR;
        }

        return CCTSStatusCode.ALLGREEN;

    }




    public CCTSTest verifyDirector(CCTSTest cctsTest){


        // Document verification stage
        for (CCTSResult result: cctsTest.getResults()) {
            CCTSStatusCode documentVerifiedResult = verifyDocumentLegality(result);
            if (documentVerifiedResult != CCTSStatusCode.ALLGREEN) {
                //error occur
                result.getTestProgress().get(0).setTestResult(false);
                continue;
            }else{
                // pass
                // set stage result
                result.getTestProgress().get(0).setTestResult(true);
            }


            // Path Construction and Verification Stage
            CCTSStatusCode pathResultCode =  pathVerification(result);
            if (pathResultCode != CCTSStatusCode.ALLGREEN) {
                //error occur
                result.getTestProgress().get(1).setTestResult(false);
                continue;
            }else {
                // pass
                result.getTestProgress().get(1).setTestResult(true);
            }

        }

        return cctsTest;
    }




    private CCTSStatusCode documentTitleVerifier(CCTSTest cctstest) {
        HashSet<String> documentTitles = new HashSet<>();
        ArrayList<CCTSResult> results = cctstest.getResults();
        for (CCTSResult result: results) {
            documentTitles.add(result.getDocument().getTitle());
        }

        // not same size = duplicated title occurred
        if(results.size() != documentTitles.size()){

            System.out.println("CCTS documents have duplicated title!");
            return CCTSStatusCode.CCTSDOCUMENT_DUPLICATED_TITLE_ERROR;
        }
        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode  verifyDocumentLegality(CCTSResult result) {
        // specify current doc
        CCTSDocument cctsDocument = result.getDocument();

        // verify document properties are not null
        if (cctsDocument.getTitle() == null
                || cctsDocument.getCCTSVersion() == null
                || cctsDocument.getStartAt() == null
                || cctsDocument.getStates() == null) {
            System.out.println("CCTS document properties are not complete!");
            result.addDocumentVerificationStageError(CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR);
            return CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
        }

        // verify states
        // state name should be unique in documents
        CCTSStatusCode isStatesValid = stateUniqueChecker(cctsDocument);
        if (isStatesValid != CCTSStatusCode.ALLGREEN) {
            result.addDocumentVerificationStageError(isStatesValid);
            return isStatesValid;
        }

        // properties check
        CCTSStatusCode isPropertiesValid = propertiesChecker(cctsDocument);
        if (isPropertiesValid != CCTSStatusCode.ALLGREEN) {
            result.addDocumentVerificationStageError(isPropertiesValid);
            return isPropertiesValid;
        }


        return CCTSStatusCode.ALLGREEN;


    }


    private CCTSStatusCode pathVerification(CCTSResult result) {

        CCTSDocument cctsDocument = result.getDocument();
        // path validity check
        CCTSStatusCode isPathValid = pathChecker(cctsDocument);
        if(isPathValid != CCTSStatusCode.ALLGREEN) {
            result.addPathConstructionAndVerificationError(isPathValid);
            return isPathValid;
        }

        // each state in the path should be connected by each other (provider consumer check)
        CCTSStatusCode isConnectedStateValid = connectedStateChecker(cctsDocument);
        if(isConnectedStateValid != CCTSStatusCode.ALLGREEN) {
            result.addPathConstructionAndVerificationError(isConnectedStateValid);
            return isConnectedStateValid;
        }

        // timeSequenceLabel Legality check
        CCTSStatusCode isTimeSequenceLabelValid = timeSequenceLabelChecker(cctsDocument);
        if(isTimeSequenceLabelValid != CCTSStatusCode.ALLGREEN) {
            result.addPathConstructionAndVerificationError(isTimeSequenceLabelValid);
            return isTimeSequenceLabelValid;
        }

        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode connectedStateChecker(CCTSDocument cctsDocument) {
        //get all path
        ArrayList<ArrayList<NextState>> paths =new ArrayList<>();
        CCTSStatusCode code =  CCTSDocumentParser.pathFinder(
                cctsDocument,
                cctsDocument.findSimpleState(cctsDocument.getStartAt()),
                new ArrayList<>(),
                paths);

        if(code != CCTSStatusCode.ALLGREEN) {
            // if circular path is found, return error
            return code;
        }

        for (ArrayList<NextState> path: paths){
            for (int i = 1; i < path.size(); i++) {
                // previous nextState's consumer should be next one's provider
                NextState currentState = path.get(i);
                NextState previousState = path.get(i - 1);
                if( !currentState.getProvider().equals(previousState.getConsumer())){
                    return CCTSStatusCode.PATH_NOT_CONNECTED;
                }
            }
        }
        return CCTSStatusCode.ALLGREEN;

    }

    private CCTSStatusCode pathChecker(CCTSDocument cctsDocument) {

        // find oud if any unreached state
        CCTSStatusCode isStateReachabilityValid = stateReachabilityChecker(cctsDocument);
        if(isStateReachabilityValid != CCTSStatusCode.ALLGREEN) return isStateReachabilityValid;

        // get all paths
        ArrayList<ArrayList<NextState>> paths = new ArrayList<>();
        CCTSStatusCode isPathValid = CCTSDocumentParser.pathFinder(cctsDocument, cctsDocument.findSimpleState(cctsDocument.getStartAt()), new ArrayList<>(), paths);
        if(isPathValid != CCTSStatusCode.ALLGREEN) return isPathValid;


        if(paths.size() == 0){
            return CCTSStatusCode.NO_VALID_PATH_FOUND;
        }
        return CCTSStatusCode.ALLGREEN;

    }

    private CCTSStatusCode timeSequenceLabelChecker(CCTSDocument cctsDocument) {
        //get all delivery
        ArrayList<NextState> deliverys =  CCTSDocumentParser.findDeliveryList(cctsDocument);
        //get all timeSequenceLabel
        ArrayList<Integer> timeSequenceLabels = new ArrayList<>();
        for ( NextState delivery : deliverys){
            timeSequenceLabels.add(delivery.getTimeSequenceLabel());
        }
        // ninja code
//        documentParser.findDeliveryList(cctsDocument).stream().map( delivery ->  delivery.getTimeSequenceLabel()).collect(Collectors.toCollection(ArrayList::new));


        //check timeSequenceLabel is unique
        HashSet<Integer> timeSequenceLabelSet = new HashSet<>(timeSequenceLabels);
        if(timeSequenceLabelSet.size() != timeSequenceLabels.size()){
            System.out.println("CCTS document has duplicated timeSequenceLabel!");
            return CCTSStatusCode.PATH_TIMESEQUENCE_LABEL_NOT_UNIQUE;
        }

        // check timeSequenceLabel is increased in path
        ArrayList<ArrayList<NextState>> paths = new ArrayList<>();
        CCTSDocumentParser.pathFinder(cctsDocument, cctsDocument.findSimpleState(cctsDocument.getStartAt()), new ArrayList<>(), paths);
        for ( ArrayList<NextState> path :  paths){
            for (int i = 1; i < path.size(); i++) {
                // should be increased
                if(path.get(i).getTimeSequenceLabel() <= path.get(i-1).getTimeSequenceLabel()){
                    System.out.println("CCTS document has timeSequenceLabel not increased in path!");
                    return CCTSStatusCode.PATH_TIMESEQUENCELABEL_NOT_INCREASED;
                }
            }
        }

        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode stateUniqueChecker(CCTSDocument cctsDocument) {
        HashSet<String> stateNamesSet = new HashSet<>();
        for (SimpleState state : cctsDocument.getStates()) {
            stateNamesSet.add(state.getStateName());
        }
        if (stateNamesSet.size() != cctsDocument.getStates().size()) {
            // duplicate state name
            System.out.println("CCTS document states have duplicate state name!");
            return CCTSStatusCode.DOCUMENT_DUPLICATED_STATE_NAME_ERROR;
        }
        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode propertiesChecker(CCTSDocument cctsDocument) {
        // verify simpleState's properties are not null
        boolean isValidSimpleState = true;
        for (SimpleState simpleState : cctsDocument.getStates()) {
            if( !verifySimpleStateLegality(simpleState) ){
                isValidSimpleState = false;
            }
        }

        // verify NextStates properties is legal (not null)
        boolean isValidNextState = true;
        for (NextState state : CCTSDocumentParser.findDeliveryList(cctsDocument)) {
            if( !verifyNextStateLegality(state)){
                isValidNextState = false;
            }
        }

        if( !(isValidSimpleState && isValidNextState)) {
            return CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
        }



        // verify nextState name should be found in all states set.
        for ( NextState nextState : CCTSDocumentParser.findDeliveryList(cctsDocument)) {
            if(cctsDocument.findSimpleState(nextState.getStateName()) == null){
                System.out.println("CCTS document nextState name is not found in all states!");
                return CCTSStatusCode.CCTSDOCUMENT_STATE_NAME_NOT_FOUND;
            }
        }




        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode stateReachabilityChecker(CCTSDocument cctsDocument) {
        // find out if any rest of state is reachable
        // traversal all potential valid path.
        ArrayList<ArrayList<NextState>> traversalPaths = new ArrayList<>();
        // start from initial state
        documentParser.pathFinder(cctsDocument, cctsDocument.findSimpleState(cctsDocument.getStartAt()), new ArrayList<>(), traversalPaths);

        // check if no valid state or not
        if(traversalPaths.size() == 0) {
            // no valid path found
            return CCTSStatusCode.NO_VALID_PATH_FOUND;
        }

        //check if any state is not reachable
        HashSet<String> reachableStates = new HashSet<>();
        for (ArrayList<NextState> path : traversalPaths) {
            for (NextState nextState  : path) {
                reachableStates.add(nextState.getStateName());
            }
        }

        HashSet<String> allDeliverys = new HashSet<>();
        for (NextState nextState : documentParser.findDeliveryList(cctsDocument)) {
            allDeliverys.add(nextState.getStateName());
        }
        if(reachableStates.size() != allDeliverys.size()) {
            return CCTSStatusCode.CCTSDOCUMENT_ERROR_STATE_NOT_REACHABLE;
        }
        return CCTSStatusCode.ALLGREEN;
    }





    private boolean verifyNextStateLegality(NextState state) {
        // required properties null
        if (state.getStateName() != null &&
                state.getConsumer() != null &&
                state.getProvider() != null &&
                state.getTestCaseId() != null &&
                state.getTimeSequenceLabel() != null) return true;
        else return false;
    }

    private boolean verifySimpleStateLegality(SimpleState simpleState) {
        // all required properties are not null
        if (simpleState.getStateName() == null) {
            return false;
        }
        // nextState and options should be mutually exclusive
        if (simpleState.getNextState() != null && simpleState.getOptions() == null && !simpleState.isEnd()) {
            // nextState is not null, but options is null.
            // should not be end state
            // valid nextState branch
            return true;
        } else if (simpleState.getNextState() == null && simpleState.getOptions() != null && !simpleState.isEnd()) {
            // options is not null, but nextState is null
            // should not be end state
            // valid options branch
            return true;
        } else if (simpleState.getNextState() == null && simpleState.getOptions() == null && simpleState.isEnd()) {
            // end state
            // should not hava nextState and options
            return true;
        } else {
            //WTF
            System.out.println("CCTS document states contain invalid state!");
            return false;
        }

    }


}
