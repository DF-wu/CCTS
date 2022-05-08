package tw.dfder.ccts.entity.cctsresultmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;

@Document(collection = "CCTSResult")
public class CCTSResult {
    @Id
    private String id;

    @Field
    private boolean testResult;


    @Field
    private CCTSDocument document;

    @Field
    private CCTSStatusCode documentStageError;

    private ArrayList<CCTSStatusCode> documentVerifiedResult;

    // match result of delivery and eventlog
    @Field
    private ArrayList<CCTSResultRecord> resultBetweenDeliveryAndEventLogs;

    // match result of delivery and it's contract from pact broker
    @Field
    private ArrayList<CCTSResultRecord> resultBetweenDeliveryAndContract;
    @Field
    private Map<String, CCTSStatusCode> pathVerificationResults;

    @Field
    private Map<String, CCTSStatusCode> contractVerificationResults;

    @Field
    private Map<String, CCTSStatusCode> deliveryVerificationResults;


    @Field
    private ArrayList<CCTSTestCase> testProgress;


    @Field
    private final ArrayList<CCTSResultRecord> passedList ;

    @Field
    private final ArrayList<CCTSResultRecord> failedList ;


    public CCTSResult(CCTSDocument document) {
        this.document = document;
        this.resultBetweenDeliveryAndEventLogs = new ArrayList<>();
        this.resultBetweenDeliveryAndContract = new ArrayList<>();
        this.contractVerificationResults = new Hashtable<>();
        this.passedList = new ArrayList<>();
        this.failedList = new ArrayList<>();
        this.pathVerificationResults = new Hashtable<>();
        this.testProgress = new ArrayList<>();
        List<CCTSTestCase> list = Arrays.asList(
                new CCTSTestCase(CCTSTestStage.PREPARE_DOCUMENT_STAGE, false),
                new CCTSTestCase(CCTSTestStage.DOCUMENT_STAGE, false),
                new CCTSTestCase(CCTSTestStage.EVENTLOG_STAGE, false),
                new CCTSTestCase(CCTSTestStage.PATH_STAGE, false),
                new CCTSTestCase(CCTSTestStage.CONTRACT_STAGE, false),
                new CCTSTestCase(CCTSTestStage.CONTRACT_TEST_STAGE, false));
        this.testProgress.addAll(list);

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
        #### Document: $
        + stateName: $
            + provider: $
            + consumer: $
            + testCaseId: $

        #### Contract Verification
        + service:
        ...
        ### Failure
        #### Document: $
        + stateName: $
            + provider: $
            + consumer: $
            + testCaseId: $
            + failure reason: $

        #### Contract Verification
        + Service:
     */
    public String checkOutReportMessageMD(){


        String outputMessage = "";
        outputMessage = "# CCTS Test Report" + lineSeparator();
        // based on hackmd syntax
        outputMessage = outputMessage + "[TOC]" + lineSeparator();
        outputMessage = outputMessage +"## Information" + lineSeparator();
        outputMessage = outputMessage + "+ Test Result: " + (testResult ? "Pass!!!" : "Fail.") + lineSeparator();
        outputMessage = outputMessage + "+ Test Time: " + LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + lineSeparator();
        outputMessage = outputMessage + "+ Passed delivery: " + passedList.size() + lineSeparator();
        outputMessage = outputMessage + "+ Failure delivery: " + failedList.size() + lineSeparator();
        outputMessage = outputMessage + "## Test detail" + lineSeparator();
        outputMessage = outputMessage + "### Potential Path: " + lineSeparator();
        for (String s : pathVerificationResults.keySet()) {
            outputMessage = outputMessage + "+ " + s + lineSeparator();
        }
        outputMessage = outputMessage + "### Pass" + lineSeparator();
        outputMessage = generateResultEntityMD(outputMessage, passedList, true);
        outputMessage = outputMessage + "#### Contract Verification" + lineSeparator();
        outputMessage = generateContractVerificationResultEntityMD(outputMessage, true);
        outputMessage = outputMessage + "#### Path:" + lineSeparator();
        outputMessage = generateResultPathEntityMD(outputMessage, true);
        outputMessage = outputMessage + "---" + lineSeparator();
        outputMessage = outputMessage + "### Failure" + lineSeparator();
        outputMessage = generateResultEntityMD(outputMessage, failedList, false);
        outputMessage = outputMessage + "#### Contract Verification" + lineSeparator();
        outputMessage = generateContractVerificationResultEntityMD(outputMessage, false);
        outputMessage = outputMessage + "#### Path:" + lineSeparator();
        outputMessage = generateResultPathEntityMD(outputMessage, false);


        return outputMessage;
    }

    private String generateResultPathEntityMD(String outputMessage, boolean isPassed) {
        String msg = outputMessage;
        for (String s : pathVerificationResults.keySet()){
            if(isPassed){
                if(pathVerificationResults.get(s) == CCTSStatusCode.ALLGREEN){
                    msg = msg + "+ " + s + lineSeparator();
                }
            }else{
                if(pathVerificationResults.get(s) != CCTSStatusCode.ALLGREEN){
                    msg = msg + "+ " + s  + lineSeparator();
                    msg = msg + "   + Reason:  "+ pathVerificationResults.get(s) + lineSeparator();
                }
            }
        }
        return msg;
    }


    private void gernerateFinalPassedAndFailList() {
        for(CCTSResultRecord n : Stream.concat(resultBetweenDeliveryAndEventLogs.stream(), resultBetweenDeliveryAndContract.stream())
                .collect(Collectors.toList())
        ){
            if(n.getResultCode().equals(CCTSStatusCode.ALLGREEN)){
                passedList.add(n);
            }else {
                failedList.add(n);
            }
        }
    }

    public Boolean checkOut(){
        gernerateFinalPassedAndFailList();
        boolean flag = false;
        for (CCTSStatusCode code: contractVerificationResults.values()) {
            if(code != CCTSStatusCode.ALLGREEN){
                flag = false;
                this.testResult = flag;
                return flag;
            }
        }
        //  path check
        for (CCTSStatusCode code : pathVerificationResults.values()) {
            if(code != CCTSStatusCode.ALLGREEN){
                flag = false;
            }
        }

        if (failedList.size() == 0 ) {
            flag = true;
            this.testResult = flag;
            return flag;

        }else {
            flag = false;
            this.testResult = false;
            return false;
        }

    }


    // collect documented result map .
    private String generateContractVerificationResultEntityMD(String outputMessage, boolean isPassed){
        String resultMessage = outputMessage;
        for (String service  : contractVerificationResults.keySet()) {
            if(isPassed){
                if(contractVerificationResults.get(service).equals(CCTSStatusCode.ALLGREEN) ){
                    resultMessage = resultMessage + "+ "+ service + lineSeparator();
                }
            }else {
                if(!contractVerificationResults.get(service).equals(CCTSStatusCode.ALLGREEN) ){
                    resultMessage = resultMessage + "+ "+ service + ": " + contractVerificationResults.get(service) + lineSeparator();
                }
            }
        }

        return resultMessage;
    }

    private HashMap<String, ArrayList<CCTSResultRecord>> documentedResultsTogether(ArrayList<CCTSResultRecord> records ){
        HashMap<String, ArrayList<CCTSResultRecord>> documentedResults = new HashMap<>();
        // collect documents name set
        HashSet<String> documentNameSet = new HashSet<>();
        for (CCTSResultRecord rr : records) {
            documentNameSet.add(rr.getDocumentTitle());
        }
        // add entity to map
        for (String document : documentNameSet) {
            documentedResults.put(document, new ArrayList<>());
        }
        // same document name with ResultRecords
        for (CCTSResultRecord rr : records) {
            documentedResults.get(rr.getDocumentTitle()).add(rr);
        }

        return documentedResults;
    }

    private String generateResultEntityMD(String msg, ArrayList<CCTSResultRecord> list, boolean isPassed) {
        HashMap<String, ArrayList<CCTSResultRecord>> documentedResultRecordMap = documentedResultsTogether(list);
        for (String documentName : documentedResultRecordMap.keySet()) {
            msg = msg + "#### Document: " + documentName + lineSeparator();
            ArrayList<CCTSResultRecord> resultRecords = documentedResultRecordMap.get(documentName);
            for (CCTSResultRecord rr : resultRecords) {
                msg = msg + "+ State Name: " + rr.getDelivery().getStateName() + lineSeparator();
                msg = msg + "    + Provider: " + rr.getDelivery().getProvider() + lineSeparator();
                msg = msg + "    + Consumer: " + rr.getDelivery().getConsumer() + lineSeparator();
                msg = msg + "    + TestCaseId: " + rr.getDelivery().getTestCaseId() + lineSeparator();
                if(!isPassed){
                    // if failed, show the error message
                    msg = msg + "    + Failure message: " + rr.getResultCode().getMessage() + lineSeparator();

                }
            }
        }

        return msg;
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
        outputMessage = outputMessage + "CCTS Test Result: " + (testResult ? "Passed!": "Not.") + lineSeparator();
        outputMessage = outputMessage + "Passed: " + lineSeparator();

        // passed

        for (CCTSResultRecord result: resultBetweenDeliveryAndEventLogs) {
            if(result.getResultCode() == CCTSStatusCode.ALLGREEN){
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
            if(result.getResultCode() == CCTSStatusCode.ALLGREEN){
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
        outputMessage = outputMessage + "Errors:" + lineSeparator();
        for (CCTSResultRecord result : resultBetweenDeliveryAndEventLogs) {
            if(result.getResultCode() != CCTSStatusCode.ALLGREEN){
                String msg = generateMessageEntity(
                        result.getDocumentTitle(),
                        result.getDelivery().getStateName(),
                        result.getDelivery().getProvider(),
                        result.getDelivery().getConsumer(),
                        result.getDelivery().getTestCaseId(),
                        result.getResultCode().getMessage()
                );
                outputMessage = outputMessage + msg;
            }
        }

        for (CCTSResultRecord result : resultBetweenDeliveryAndContract) {
            if(result.getResultCode() != CCTSStatusCode.ALLGREEN){
                String msg = generateMessageEntity(
                        result.getDocumentTitle(),
                        result.getDelivery().getStateName(),
                        result.getDelivery().getProvider(),
                        result.getDelivery().getConsumer(),
                        result.getDelivery().getTestCaseId(),
                        result.getResultCode().getMessage()
                );
                outputMessage = outputMessage + msg;
            }
        }

        return outputMessage;
    }

    private String generateMessageEntity(String document, String stateName, String provider, String consumer, String testCaseId){
        String msg =
                      "  DocumentTitle: " + document  + lineSeparator()
                    + "    stateName: " + stateName + lineSeparator()
                    + "      provider: " + provider + lineSeparator()
                    + "      consumer: " + consumer + lineSeparator()
                    + "      testCaseId: " + testCaseId + lineSeparator();
        return msg;
    }

    private String generateMessageEntity(String document, String stateName, String provider, String consumer, String testCaseId, String errorMessage){
        String msg =
                      "  DocumentTitle: " + document  + lineSeparator()
                    + "    stateName: " + stateName + lineSeparator()
                    + "      provider: " + provider + lineSeparator()
                    + "      consumer: " + consumer + lineSeparator()
                    + "      testCaseId: " + testCaseId + lineSeparator()
                    + "      error message: " + errorMessage + lineSeparator();
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

    public Map<String, CCTSStatusCode> getContractVerificationResults() {
        return contractVerificationResults;
    }

    public void setContractVerificationResults(Map<String, CCTSStatusCode> contractVerificationResults) {
        this.contractVerificationResults = contractVerificationResults;
    }

    public Map<String, CCTSStatusCode> getPathVerificationResults() {
        return pathVerificationResults;
    }

    public void setPathVerificationResults(Map<String, CCTSStatusCode> pathVerificationResults) {
        this.pathVerificationResults = pathVerificationResults;
    }

    public CCTSDocument getDocument() {
        return document;
    }

    public void setDocument(CCTSDocument document) {
        this.document = document;
    }

    public ArrayList<CCTSTestCase> getTestProgress() {
        return testProgress;
    }

    public void setTestProgress(ArrayList<CCTSTestCase> testProgress) {
        this.testProgress = testProgress;
    }

    public CCTSStatusCode getDocumentStageError() {
        return documentStageError;
    }

    public void setDocumentStageError(CCTSStatusCode documentStageError) {
        this.documentStageError = documentStageError;
    }
}
