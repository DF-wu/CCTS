package tw.dfder.ccts.entity.cctsresultmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Stream;

@Document(collection = "CCTSResult")
public class CCTSResult {
    @Id
    private String id;

    @Field
    private boolean testResult;
    @Field
    private final ArrayList<CCTSDocument> relatedDocuments ;

    // match result of delivery and eventlog
    @Field
    private ArrayList<CCTSResultRecord> resultBetweenDeliveryAndEventLogs;

    // match result of delivery and it's contract from pact broker
    @Field
    private ArrayList<CCTSResultRecord> resultBetweenDeliveryAndContract;

    @Field
    private Map<String, CCTSStatusCode> contractVerificationErrors;

    public CCTSResult(ArrayList<CCTSDocument> relatedDocuments) {
        this.relatedDocuments = relatedDocuments;
        this.resultBetweenDeliveryAndEventLogs = new ArrayList<>();
        this.resultBetweenDeliveryAndContract = new ArrayList<>();
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

        for (CCTSResultRecord result: resultBetweenDeliveryAndEventLogs) {
            if(result.getErrorCode() == CCTSStatusCode.ALLGREEN){
                String msg = generateMessageEntity(
                        result.getDocumentTitle(),
                        result.getDelivery().getStateName(),
                        result.getDelivery().getProvider(),
                        result.getDelivery().getConsumer(),
                        result.getDelivery().getTestCaseId()
                );
                outputMessage = outputMessage + msg;
            }
        }

        for (CCTSResultRecord result : resultBetweenDeliveryAndContract) {
            if(result.getErrorCode() == CCTSStatusCode.ALLGREEN){
                String msg = generateMessageEntity(
                        result.getDocumentTitle(),
                        result.getDelivery().getStateName(),
                        result.getDelivery().getProvider(),
                        result.getDelivery().getConsumer(),
                        result.getDelivery().getTestCaseId()
                );
                outputMessage = outputMessage + msg;
            }
        }


        // errors
        outputMessage = outputMessage + "Errors:" + System.lineSeparator();
        for (CCTSResultRecord result : resultBetweenDeliveryAndEventLogs) {
            if(result.getErrorCode() != CCTSStatusCode.ALLGREEN){
                String msg = generateMessageEntity(
                        result.getDocumentTitle(),
                        result.getDelivery().getStateName(),
                        result.getDelivery().getProvider(),
                        result.getDelivery().getConsumer(),
                        result.getDelivery().getTestCaseId(),
                        result.getErrorCode().getInfoMessage()
                );
                outputMessage = outputMessage + msg;
            }
        }

        for (CCTSResultRecord result : resultBetweenDeliveryAndContract) {
            if(result.getErrorCode() != CCTSStatusCode.ALLGREEN){
                String msg = generateMessageEntity(
                        result.getDocumentTitle(),
                        result.getDelivery().getStateName(),
                        result.getDelivery().getProvider(),
                        result.getDelivery().getConsumer(),
                        result.getDelivery().getTestCaseId(),
                        result.getErrorCode().getInfoMessage()
                );
                outputMessage = outputMessage + msg;
            }
        }

        return outputMessage;
    }

    private String generateMessageEntity(String document, String stateName, String provider, String consumer, String testCaseId){
        String msg =
                      "  DocumentTitle: " + document  + System.lineSeparator()
                    + "    stateName: " + stateName + System.lineSeparator()
                    + "      provider: " + provider + System.lineSeparator()
                    + "      consumer: " + consumer + System.lineSeparator()
                    + "      testCaseId: " + testCaseId + System.lineSeparator();
        return msg;
    }

    private String generateMessageEntity(String document, String stateName, String provider, String consumer, String testCaseId, String errorMessage){
        String msg =
                      "  DocumentTitle: " + document  + System.lineSeparator()
                    + "    stateName: " + stateName + System.lineSeparator()
                    + "      provider: " + provider + System.lineSeparator()
                    + "      consumer: " + consumer + System.lineSeparator()
                    + "      testCaseId: " + testCaseId + System.lineSeparator()
                    + "      error message: " + errorMessage + System.lineSeparator();
        return msg;
    }

    // below for accessors


    public boolean isTestResult() {
        return testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }

    public ArrayList<CCTSResultRecord> getResultBetweenDeliveryAndEventLogs() {
        return resultBetweenDeliveryAndEventLogs;
    }

    public void setResultBetweenDeliveryAndEventLogs(ArrayList<CCTSResultRecord> resultBetweenDeliveryAndEventLogs) {
        this.resultBetweenDeliveryAndEventLogs = resultBetweenDeliveryAndEventLogs;
    }

    public ArrayList<CCTSResultRecord> getResultBetweenDeliveryAndContract() {
        return resultBetweenDeliveryAndContract;
    }

    public void setResultBetweenDeliveryAndContract(ArrayList<CCTSResultRecord> resultBetweenDeliveryAndContract) {
        this.resultBetweenDeliveryAndContract = resultBetweenDeliveryAndContract;
    }

    public Map<String, CCTSStatusCode> getContractVerificationErrors() {
        return contractVerificationErrors;
    }

    public void setContractVerificationErrors(Map<String, CCTSStatusCode> contractVerificationErrors) {
        this.contractVerificationErrors = contractVerificationErrors;
    }
}
