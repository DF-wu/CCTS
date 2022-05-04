package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.cctsdocumentmodel.SimpleState;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.EventLogRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static tw.dfder.ccts.entity.CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;

@Service("DocumentVerifier")
public class DocumentVerifier {
    public String currentDocumentTitle = "";
    private final CCTSDocumentParser documentParser;

    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;

    public CCTSStatusCode documentVerifiedResult;
    public String prepareDocumentErrorMessage = "";

    @Autowired
    public DocumentVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
    }

    public CCTSTest verifyDirector(){

        ArrayList<CCTSDocument> documents = (ArrayList<CCTSDocument>) cctsDocumentRepository.findAll();
        CCTSTest cctsTest = new CCTSTest(documents);
        currentDocumentTitle = "";
        documentVerifiedResult = null;

        for (CCTSResult result: cctsTest.getResults()) {
            documentVerifiedResult = verifyDocumentLegality(result.getDocument());
            if (documentVerifiedResult != CCTSStatusCode.ALLGREEN) {
                result.setDocumentStageError(documentVerifiedResult);
            }
        }

        currentDocumentTitle = "" ;
        return cctsTest;
    }

    public CCTSStatusCode prepareDocumentVerify() {
        prepareDocumentErrorMessage = "";
        try {
            ArrayList<CCTSDocument> documents = documentParser.parseAllCCTSProfilesAndSave2DB();
            //verify all documents have unique title
            documentVerifiedResult = documentTitleVerifier(documents);
            if (documentVerifiedResult != CCTSStatusCode.ALLGREEN) {
                return CCTSStatusCode.CCTSDOCUMENT_DUPLICATED_TITLE_ERROR;
            }
            return CCTSStatusCode.ALLGREEN;

        } catch (Exception e) {
            //  return error message to frontend
            System.out.println("CCTS Documenet parse error");
            e.printStackTrace();
            prepareDocumentErrorMessage = e.getMessage();
            documentVerifiedResult = CCTSStatusCode.CCTSDOCUMENT_PARSE_ERROR;
            return documentVerifiedResult;
        }

    }



    private CCTSStatusCode documentTitleVerifier(ArrayList<CCTSDocument> documents) {
        HashSet<String> documentTitles = new HashSet<>();
        for (CCTSDocument document : documents){
            documentTitles.add(document.getTitle());
        }
        if(documents.size() != documentTitles.size()){
            System.out.println("CCTS documents have duplicated title!");
            return CCTSStatusCode.CCTSDOCUMENT_DUPLICATED_TITLE_ERROR;
        }
        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode  verifyDocumentLegality(CCTSDocument cctsDocument) {
        // specify current doc
        currentDocumentTitle = cctsDocument.getTitle();

        // verify document properties are not null
        if (cctsDocument.getTitle() == null
                || cctsDocument.getCCTSversion() == null
                || cctsDocument.getStartAt() == null
                || cctsDocument.getStates() == null) {
            System.out.println("CCTS document properties are not complete!");
            return CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
        }

        // verify states
        // state name should be unique in documents
        CCTSStatusCode isStatesValid = stateUniqueChecker(cctsDocument);
        if (isStatesValid != CCTSStatusCode.ALLGREEN) return isStatesValid;

        // properties check
        CCTSStatusCode isPropertiesValid = propertiesChecker(cctsDocument);
        if (isPropertiesValid != CCTSStatusCode.ALLGREEN) return isPropertiesValid;


        // path valid check
        CCTSStatusCode isPathValid = pathChecker(cctsDocument);
        if(isPathValid != CCTSStatusCode.ALLGREEN) return isPathValid;

        // timeSequenceLabel Legality check
        CCTSStatusCode isTimeSequenceLabelValid = timeSequenceLabelChecker(cctsDocument);
        if(isTimeSequenceLabelValid != CCTSStatusCode.ALLGREEN) return isTimeSequenceLabelValid;

        return CCTSStatusCode.ALLGREEN;


    }

    private CCTSStatusCode pathChecker(CCTSDocument cctsDocument) {

        // find oud if any unreached state
        CCTSStatusCode isStateReachabilityValid = stateReachabilityChecker(cctsDocument);
        if(isStateReachabilityValid != CCTSStatusCode.ALLGREEN) return isStateReachabilityValid;

        // get all paths
        ArrayList<ArrayList<NextState>> paths = new ArrayList<>();
        documentParser.pathFinder(cctsDocument, cctsDocument.findSimpleState(cctsDocument.getStartAt()), new ArrayList<>(), paths);


        if(paths.size() == 0){
            return CCTSStatusCode.NO_VALID_PATH_FOUND;
        }
        return CCTSStatusCode.ALLGREEN;

    }

    private CCTSStatusCode timeSequenceLabelChecker(CCTSDocument cctsDocument) {
        //get all delivery
        ArrayList<NextState> deliverys =  documentParser.findDeliveryList(cctsDocument);
        //get all timeSequenceLabel
        ArrayList<Integer> timeSequenceLabels = new ArrayList<>();
        for ( NextState delivery : deliverys){
            documentParser.findDeliveryList(cctsDocument);
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
        documentParser.pathFinder(cctsDocument, cctsDocument.findSimpleState(cctsDocument.getStartAt()), new ArrayList<>(), paths);
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
        boolean isValidSimpleState = false;
        for (SimpleState simpleState : cctsDocument.getStates()) {
            isValidSimpleState = verifySimpleStateLegality(simpleState);
        }

        // verify NextStates properties is legal
        boolean isValidNextState = false;
        for (NextState state : documentParser.findDeliveryList(cctsDocument)) {
            isValidNextState = verifyNextStateLegality(state);
        }

        if(isValidSimpleState && isValidNextState) {
            return CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
        }


        // verify nextState name should be found in all states set.
        for ( NextState nextState : documentParser.findDeliveryList(cctsDocument)) {
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
