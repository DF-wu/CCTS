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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

import static tw.dfder.ccts.entity.CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;

@Service("DocumentVerifier")
public class DocumentVerifier {

    private final CCTSDocumentParser documentParser;

    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;

    @Autowired
    public DocumentVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
    }

    public void VerifyDirector() {
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();

        try {
            ArrayList<CCTSDocument> documents = documentParser.parseAllCCTSProfilesAndSave2DB();
        } catch (Exception e) {
            // TODO: return error message to frontend
            System.out.println("CCTS Documenet parse error");
            e.printStackTrace();
        }
    }

    private CCTSStatusCode verifyDocumentLegality(CCTSDocument cctsDocument) {
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
        if (documentDuplicatedStateNameError != null) return documentDuplicatedStateNameError;

        // properties check
        CCTSStatusCode cctsdocumentRequiredPropertiesNullError = propertiesChecker(cctsDocument);
        if (cctsdocumentRequiredPropertiesNullError != null) return cctsdocumentRequiredPropertiesNullError;

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
        return null;
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
        return null;
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
