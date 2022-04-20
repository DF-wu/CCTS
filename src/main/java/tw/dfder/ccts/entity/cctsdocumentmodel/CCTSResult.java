package tw.dfder.ccts.entity.cctsdocumentmodel;

import org.springframework.data.mongodb.core.mapping.Document;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Document(collection = "CCTSResult")
public class CCTSResult {
    private boolean testResult;
    private final ArrayList<CCTSDocument> relatedDocuments ;
    private Map<NextState, CCTSStatusCode> resultBetweenPathAndEventLogs;
    private Map<NextState, CCTSStatusCode> resultBetweenPathAndContract;
    private Map<String, CCTSStatusCode> contractVerificationErrors;

    public CCTSResult(ArrayList<CCTSDocument> relatedDocuments) {
        this.relatedDocuments = relatedDocuments;
        this.resultBetweenPathAndEventLogs = new Hashtable<>();
        this.resultBetweenPathAndContract = new Hashtable<>();
        this.contractVerificationErrors = new Hashtable<>();
    }


    // below for accessors


    public boolean isTestResult() {
        return testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }

    public Map<NextState, CCTSStatusCode> getResultBetweenPathAndEventLogs() {
        return resultBetweenPathAndEventLogs;
    }

    public void setResultBetweenPathAndEventLogs(Map<NextState, CCTSStatusCode> resultBetweenPathAndEventLogs) {
        this.resultBetweenPathAndEventLogs = resultBetweenPathAndEventLogs;
    }

    public Map<NextState, CCTSStatusCode> getResultBetweenPathAndContract() {
        return resultBetweenPathAndContract;
    }

    public void setResultBetweenPathAndContract(Map<NextState, CCTSStatusCode> resultBetweenPathAndContract) {
        this.resultBetweenPathAndContract = resultBetweenPathAndContract;
    }

    public Map<String, CCTSStatusCode> getContractVerificationErrors() {
        return contractVerificationErrors;
    }

    public void setContractVerificationErrors(Map<String, CCTSStatusCode> contractVerificationErrors) {
        this.contractVerificationErrors = contractVerificationErrors;
    }
}
