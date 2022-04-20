package tw.dfder.ccts.entity.cctsresultmodel;

import org.springframework.data.mongodb.core.mapping.Document;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

@Document(collection = "CCTSResult")
public class CCTSResult {
    private boolean testResult;
    private final ArrayList<CCTSDocument> relatedDocuments ;

    private ArrayList<ResultRecord> resultBetweenPathAndEventLogs;

//    private Map<NextState, CCTSStatusCode> resultBetweenPathAndEventLogs;

    private ArrayList<ResultRecord> resultBetweenPathAndContract;
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
          Document title:
              stateName:
              consumer:
              producer:
              testCaseId:
              --------


          ...
        Errors:
          Document title:
              stateName:
              consumer:
              producer:
              testCaseId:
              Error Message:
             ...
          path. Message: error msg@ document name
          ...
         */
    public String checkOutReportMessage() {

        String outputMessage = "";
        outputMessage = outputMessage + "CCTS Test Result: " + (testResult ? "Passed!": "Not.") + System.lineSeparator();
        outputMessage = outputMessage + "Passed: " + System.lineSeparator();

        // passed

        for (ResultRecord result: resultBetweenPathAndEventLogs) {
            if(result.getErrorCode() == CCTSStatusCode.ALLGREEN){
                String msg =
                          "  DocumentTitle:" + System.lineSeparator()
                        + "    stateName: " + result.getPath().getStateName() + System.lineSeparator()
                        + "    provider: " + result.getPath().getProvider() + System.lineSeparator()
                        + "    consumer: " + result.getPath().getConsumer() + System.lineSeparator()
                        + "    testCaseId: " + result.getPath().getTestCaseId() + System.lineSeparator()
                        + "-------------" + System.lineSeparator();
                outputMessage = outputMessage + msg;
            }
        }

        for (ResultRecord result : resultBetweenPathAndContract) {
            if(result.getErrorCode() == CCTSStatusCode.ALLGREEN){
                String msg =
                        "  DocumentTitle:" + System.lineSeparator()
                      + "    stateName: " + result.getPath().getStateName() + System.lineSeparator()
                      + "    provider: " + result.getPath().getProvider() + System.lineSeparator()
                      + "    consumer: " + result.getPath().getConsumer() + System.lineSeparator()
                      + "    testCaseId: " + result.getPath().getTestCaseId() + System.lineSeparator()
                      + "-------------" + System.lineSeparator();
                outputMessage = outputMessage + msg;
            }
        }


        // errors
        outputMessage = outputMessage + "Errors:" + System.lineSeparator();
        for (ResultRecord result : resultBetweenPathAndEventLogs) {
            if(result.getErrorCode() != CCTSStatusCode.ALLGREEN){
                String msg =
                        "  DocumentTitle:" + System.lineSeparator()
                      + "    stateName: " + result.getPath().getStateName() + System.lineSeparator()
                      + "    provider: " + result.getPath().getProvider() + System.lineSeparator()
                      + "    consumer: " + result.getPath().getConsumer() + System.lineSeparator()
                      + "    testCaseId: " + result.getPath().getTestCaseId() + System.lineSeparator()
                      + "    error message: " + result.getErrorCode().getInfoMessage() + System.lineSeparator()
                      + "-------------" + System.lineSeparator();
                outputMessage = outputMessage + msg;
            }
        }

        for (ResultRecord result : resultBetweenPathAndContract) {
            if(result.getErrorCode() != CCTSStatusCode.ALLGREEN){
                String msg =
                        "  DocumentTitle:" + System.lineSeparator()
                                + "    stateName: " + result.getPath().getStateName() + System.lineSeparator()
                                + "    provider: " + result.getPath().getProvider() + System.lineSeparator()
                                + "    consumer: " + result.getPath().getConsumer() + System.lineSeparator()
                                + "    testCaseId: " + result.getPath().getTestCaseId() + System.lineSeparator()
                                + "    error message: " + result.getErrorCode().getInfoMessage() + System.lineSeparator()
                                + "-------------" + System.lineSeparator();
                outputMessage = outputMessage + msg;
            }
        }

        return outputMessage;
    }



    // below for accessors


    public boolean isTestResult() {
        return testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }

    public ArrayList<ResultRecord> getResultBetweenPathAndEventLogs() {
        return resultBetweenPathAndEventLogs;
    }

    public void setResultBetweenPathAndEventLogs(ArrayList<ResultRecord> resultBetweenPathAndEventLogs) {
        this.resultBetweenPathAndEventLogs = resultBetweenPathAndEventLogs;
    }

    public ArrayList<ResultRecord> getResultBetweenPathAndContract() {
        return resultBetweenPathAndContract;
    }

    public void setResultBetweenPathAndContract(ArrayList<ResultRecord> resultBetweenPathAndContract) {
        this.resultBetweenPathAndContract = resultBetweenPathAndContract;
    }

    public Map<String, CCTSStatusCode> getContractVerificationErrors() {
        return contractVerificationErrors;
    }

    public void setContractVerificationErrors(Map<String, CCTSStatusCode> contractVerificationErrors) {
        this.contractVerificationErrors = contractVerificationErrors;
    }
}
