package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.EventLog;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.cctsdocumentmodel.SimpleState;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.EventLogRepository;

import java.util.ArrayList;
import java.util.HashMap;
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

    public CCTSStatusCode VerifyDirector() {
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();

        try {
            ArrayList<CCTSDocument> documents = documentParser.parseAllCCTSProfilesAndSave2DB();

            //verify all documents have unique title
            CCTSStatusCode cctsdocumentDuplicatedTitleError = documentTitleVerifier(documents);
            if (cctsdocumentDuplicatedTitleError != CCTSStatusCode.ALLGREEN) return cctsdocumentDuplicatedTitleError;

            //
            for (CCTSDocument cctsDocument : documents) {
                CCTSStatusCode documentVerifiedResult = verifyDocumentLegality(cctsDocument);
                if (documentVerifiedResult != CCTSStatusCode.ALLGREEN) {
                    return documentVerifiedResult;
                }
            }
            // all pass
            currentDocumentTitle = "" ;
            return CCTSStatusCode.ALLGREEN;

        } catch (Exception e) {
            // TODO: return error message to frontend
            System.out.println("CCTS Documenet parse error");
            e.printStackTrace();
            return CCTSStatusCode.CCTSDOCUMENT_PARSE_ERROR;
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

    private CCTSStatusCode verifyDocumentLegality(CCTSDocument cctsDocument) {
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
        CCTSStatusCode documentDuplicatedStateNameError = stateUniqueChecker(cctsDocument);
        if (documentDuplicatedStateNameError != CCTSStatusCode.ALLGREEN) return documentDuplicatedStateNameError;

        // properties check
        CCTSStatusCode cctsdocumentRequiredPropertiesNullError = propertiesChecker(cctsDocument);
        if (cctsdocumentRequiredPropertiesNullError != CCTSStatusCode.ALLGREEN) return cctsdocumentRequiredPropertiesNullError;

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
                return CCTSStatusCode.CCTSDOCUMENT_ERROR_STATENAME_NOT_FOUND;
            }
        }

        // find oud if any unreached state
        CCTSStatusCode cctsdocumentErrorStateNotReachable = unRecheableStateChecker(cctsDocument);
        if (cctsdocumentErrorStateNotReachable != CCTSStatusCode.ALLGREEN) return cctsdocumentErrorStateNotReachable;


        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode unRecheableStateChecker(CCTSDocument cctsDocument) {
        // find out if any rest of state is reachable
        // traversal all potential valid path.
        ArrayList<ArrayList<NextState>> traversalPaths = new ArrayList<>();
        // start from initial state
        documentParser.pathFinder(cctsDocument, cctsDocument.findSimpleState(cctsDocument.getStartAt()), new ArrayList<>(), traversalPaths);

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
        if (state.getStateName() == null ||
                state.getConsumer() == null ||
                state.getProvider() == null ||
                state.getTestCaseId() == null ||
                state.getTimeSequenceLabel() == null ) {
            // required properties null
            return false;
        }
        return true;
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
            System.out.println("CCTS document states have invalid state!");
            return false;
        }

    }


}
