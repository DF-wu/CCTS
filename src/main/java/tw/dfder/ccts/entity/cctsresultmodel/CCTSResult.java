package tw.dfder.ccts.entity.cctsresultmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
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
        print out in mark down
        sample:

        # CCTS Test Result
        ## Info
        + Test Time:
        + Pass number
        + Failure number:
        [TOC]
        ## Test Results
        ### Pass
        #### $Document name
        + stateName: $
            + provider: $
            + consumer:
            + testCaseId:
        #### Contract Verification
        + service:

        ### Failure
        #### $Document name
        + stateName: $
            + provider: $
            + consumer:
            + testCaseId:
            + failure reason:
        #### Contract Verification
        + Service:
     */
    public String checkOutReportMessageMD(){
        ArrayList<CCTSResultRecord> passed = new ArrayList<>();
        ArrayList<CCTSResultRecord> failed = new ArrayList<>();

        for(CCTSResultRecord n : Stream.concat(resultBetweenDeliveryAndContract.stream(), resultBetweenDeliveryAndContract.stream())
                .collect(Collectors.toList())
        ){
            if(n.getErrorCode().equals(CCTSStatusCode.ALLGREEN)){
                passed.add(n);
            }else {
                failed.add(n);
            }
        }

        String outputMessage = "";
        outputMessage = "# CCTS Test Report" + System.lineSeparator();
        outputMessage = outputMessage +"## Information" + System.lineSeparator();
        outputMessage = outputMessage + "+ Test Time: " + LocalDate.now() + System.lineSeparator();
        outputMessage = outputMessage + "+ Pass number: " + passed.size() + System.lineSeparator();
        outputMessage = outputMessage + "+ Failure number: " + failed.size() + System.lineSeparator();
        // based on hackmd syntax
        outputMessage = outputMessage + "[TOC]" + System.lineSeparator();
        outputMessage = outputMessage + "## Test Result" + System.lineSeparator();
        outputMessage = outputMessage + "### Pass" + System.lineSeparator();
        outputMessage = generateResultPassedEntityMD(outputMessage, true);
        outputMessage = outputMessage + "### Failure" + System.lineSeparator();
        outputMessage = generateResultPassedEntityMD(outputMessage, false);

        return outputMessage;
    }


    // collect documented result map .
    private HashMap<String, ArrayList<CCTSResultRecord>> documentedResultsTogether(ArrayList<CCTSResultRecord> records1, ArrayList<CCTSResultRecord> records2 ){
        HashMap<String, ArrayList<CCTSResultRecord>> documentedResults = new HashMap<>();
        // collect documents name set
        HashSet<String> documentNameSet = new HashSet<>();
        for (CCTSResultRecord rr : Stream.concat(records1.stream(), records2.stream()).collect(Collectors.toList())) {
            documentNameSet.add(rr.getDocumentTitle());
        }
        // add entity to map
        for (String document : documentNameSet) {
            documentedResults.put(document, new ArrayList<>());
        }
        // same document name with ResultRecords
        for (CCTSResultRecord rr : Stream.concat(records1.stream(), records2.stream()).collect(Collectors.toList())) {
            documentedResults.get(rr.getDocumentTitle()).add(rr);
        }

        return documentedResults;
    }



    private String generateResultPassedEntityMD(String msg, boolean isPassed) {
        if(isPassed){
            //passed entity
            HashMap<String, ArrayList<CCTSResultRecord>> resultMap = documentedResultsTogether(resultBetweenDeliveryAndContract, resultBetweenDeliveryAndEventLogs);
            for (String documentName : resultMap.keySet()) {
                msg = msg + "#### " + documentName + System.lineSeparator();
                ArrayList<CCTSResultRecord> resultRecords = resultMap.get(documentName);
                for (CCTSResultRecord rr : resultRecords) {
                    msg = msg + "+ stateName: " + rr.getDelivery().getStateName() + System.lineSeparator();
                    msg = msg + "    + provider: " + rr.getDelivery().getProvider() + System.lineSeparator();
                    msg = msg + "    + consumer: " + rr.getDelivery().getConsumer() + System.lineSeparator();
                    msg = msg + "    + testCaseId: " + rr.getDelivery().getTestCaseId() + System.lineSeparator();
                }
            }
            return msg;
        }else {
            //failed entity
            HashMap<String, ArrayList<CCTSResultRecord>> resultMap = documentedResultsTogether(resultBetweenDeliveryAndContract, resultBetweenDeliveryAndEventLogs);
            for (String documentName : resultMap.keySet()) {
                msg = msg + "#### " + documentName + System.lineSeparator();
                ArrayList<CCTSResultRecord> resultRecords = resultMap.get(documentName);
                for (CCTSResultRecord rr : resultRecords) {
                    msg = msg + "+ stateName: " + rr.getDelivery().getStateName() + System.lineSeparator();
                    msg = msg + "    + provider: " + rr.getDelivery().getProvider() + System.lineSeparator();
                    msg = msg + "    + consumer: " + rr.getDelivery().getConsumer() + System.lineSeparator();
                    msg = msg + "    + testCaseId: " + rr.getDelivery().getTestCaseId() + System.lineSeparator();
                    msg = msg + "    + failure message: " + rr.getErrorCode().getInfoMessage() + System.lineSeparator();
                }
            }
            return msg;
        }

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
