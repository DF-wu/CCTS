package tw.dfder.ccts.entity;

import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;

import java.util.ArrayList;
import java.util.Map;

public class CCTSResult {
    private boolean testResult;
    private final ArrayList<CCTSDocument> relatedDocuments ;
    private Map<NextState, CCTSStatusCode> eventLogErrors;
    private Map<String, CCTSStatusCode> contractVerificationErrors;

    public CCTSResult(ArrayList<CCTSDocument> relatedDocuments) {
        this.relatedDocuments = relatedDocuments;
    }


    // below for accessors


    public boolean isTestResult() {
        return testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }

    public Map<NextState, CCTSStatusCode> getEventLogErrors() {
        return eventLogErrors;
    }

    public void setEventLogErrors(Map<NextState, CCTSStatusCode> eventLogErrors) {
        this.eventLogErrors = eventLogErrors;
    }

    public Map<String, CCTSStatusCode> getContractVerificationErrors() {
        return contractVerificationErrors;
    }

    public void setContractVerificationErrors(Map<String, CCTSStatusCode> contractVerificationErrors) {
        this.contractVerificationErrors = contractVerificationErrors;
    }
}
