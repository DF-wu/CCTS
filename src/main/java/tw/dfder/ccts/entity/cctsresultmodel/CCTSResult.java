package tw.dfder.ccts.entity.cctsresultmodel;

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

    private ArrayList<ErrorRecord> resultBetweenPathAndEventLogs;

//    private Map<NextState, CCTSStatusCode> resultBetweenPathAndEventLogs;

    private ArrayList<ErrorRecord> resultBetweenPathAndContract;
//    private Map<NextState, CCTSStatusCode> resultBetweenPathAndContract;


    private Map<String, CCTSStatusCode> contractVerificationErrors;

    public CCTSResult(ArrayList<CCTSDocument> relatedDocuments) {
        this.relatedDocuments = relatedDocuments;
        this.resultBetweenPathAndEventLogs = new ArrayList<>();
        this.resultBetweenPathAndContract = new ArrayList<>();
        this.contractVerificationErrors = new Hashtable<>();
    }


    /*
        Sample output
        CCTS Test result: Pass or Not
        Passed:
          path@document name
          ...
        Errors:
          error msg@ document name
          ...
         */
    public String checkOutReportMessage() {
        return null;



    }



    // below for accessors


    public boolean isTestResult() {
        return testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }

    public ArrayList<ErrorRecord> getResultBetweenPathAndEventLogs() {
        return resultBetweenPathAndEventLogs;
    }

    public void setResultBetweenPathAndEventLogs(ArrayList<ErrorRecord> resultBetweenPathAndEventLogs) {
        this.resultBetweenPathAndEventLogs = resultBetweenPathAndEventLogs;
    }

    public ArrayList<ErrorRecord> getResultBetweenPathAndContract() {
        return resultBetweenPathAndContract;
    }

    public void setResultBetweenPathAndContract(ArrayList<ErrorRecord> resultBetweenPathAndContract) {
        this.resultBetweenPathAndContract = resultBetweenPathAndContract;
    }

    public Map<String, CCTSStatusCode> getContractVerificationErrors() {
        return contractVerificationErrors;
    }

    public void setContractVerificationErrors(Map<String, CCTSStatusCode> contractVerificationErrors) {
        this.contractVerificationErrors = contractVerificationErrors;
    }
}
