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

@Document(collection = "CCTSResult")
public class CCTSResult {
    @Id
    private String id;

    @Field
    private CCTSStatusCode verifiedResult ;
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
    private Map<String, CCTSStatusCode> contractVerificationResults;

    @Field
    private ArrayList<CCTSResultRecord> passedList ;

    @Field
    private ArrayList<CCTSResultRecord> failedList ;


    // key is case sequence
    @Field
    private Map<String, CCTSStatusCode> caseSequenceResults ;

    public CCTSResult(ArrayList<CCTSDocument> relatedDocuments) {
        this.relatedDocuments = relatedDocuments;
        this.resultBetweenDeliveryAndEventLogs = new ArrayList<>();
        this.resultBetweenDeliveryAndContract = new ArrayList<>();
        this.contractVerificationResults = new Hashtable<>();
        this.passedList = new ArrayList<>();
        this.failedList = new ArrayList<>();
        this.caseSequenceResults = new HashMap<>();
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
        outputMessage = "# CCTS Test Report" + System.lineSeparator();
        // based on hackmd syntax
        outputMessage = outputMessage + "[TOC]" + System.lineSeparator();
        outputMessage = outputMessage +"## Information" + System.lineSeparator();
        outputMessage = outputMessage + "+ Test Result: " + (testResult ? "Pass!!!" : "Fail.") + System.lineSeparator();
        outputMessage = outputMessage + "+ Test Time: " + LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + System.lineSeparator();
        outputMessage = outputMessage + "+ Passed delivery: " + passedList.size() + System.lineSeparator();
        outputMessage = outputMessage + "+ Failure delivery: " + failedList.size() + System.lineSeparator();
        outputMessage = outputMessage + "## Test detail" + System.lineSeparator();
        outputMessage = outputMessage + "### Pass" + System.lineSeparator();
        outputMessage = generateResultEntityMD(outputMessage, passedList, true);
        outputMessage = outputMessage + "#### Contract Verification" + System.lineSeparator();
        outputMessage = generateContractVerificationResultEntityMD(outputMessage, true);
        outputMessage = outputMessage + "#### Case Sequence Verification" + System.lineSeparator();
        outputMessage = generateCaseSequenceResultEntityMD(outputMessage, true);
        outputMessage = outputMessage + "---" + System.lineSeparator();
        outputMessage = outputMessage + "### Failure" + System.lineSeparator();
        outputMessage = generateResultEntityMD(outputMessage, failedList, false);
        outputMessage = outputMessage + "#### Contract Verification" + System.lineSeparator();
        outputMessage = generateContractVerificationResultEntityMD(outputMessage, false);
        outputMessage = outputMessage + "#### Case Sequence Verification" + System.lineSeparator();
        outputMessage = generateCaseSequenceResultEntityMD(outputMessage, false);

        return outputMessage;
    }

    private String generateCaseSequenceResultEntityMD(String outputMessage, boolean flag) {
        String msgT = "";
        String msgF = "";

        for(String key : caseSequenceResults.keySet()){
            if( caseSequenceResults.get(key).equals(CCTSStatusCode.ALLGREEN)){
                // passed
                msgT = msgT +   "+ " + key + System.lineSeparator();
            }else{
                // failed
                msgF = msgF +   "+ " + key + System.lineSeparator();
                msgF = msgF +   "  + Failure reason: " + caseSequenceResults.get(key).getMessage() + System.lineSeparator();
            }
        }

        if(flag){
            outputMessage = outputMessage + msgT;
        }else {
            outputMessage = outputMessage + msgF;
        }
        return outputMessage;

    }

    private void gernerateFinalPassedAndFailList() {
        for(CCTSResultRecord n : Stream.concat(resultBetweenDeliveryAndEventLogs.stream(), resultBetweenDeliveryAndContract.stream())
                .collect(Collectors.toList())
        ){
            if(n.getErrorCode().equals(CCTSStatusCode.ALLGREEN)){
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

        for(CCTSStatusCode code : caseSequenceResults.values()){
            if(code != CCTSStatusCode.ALLGREEN){
                flag = false;
                this.testResult = flag;
                return flag;
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
                    resultMessage = resultMessage + "+ "+ service + System.lineSeparator();
                }
            }else {
                if(!contractVerificationResults.get(service).equals(CCTSStatusCode.ALLGREEN) ){
                    resultMessage = resultMessage + "+ "+ service + ": " + contractVerificationResults.get(service) + System.lineSeparator();
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
            msg = msg + "#### Document: " + documentName + System.lineSeparator();
            ArrayList<CCTSResultRecord> resultRecords = documentedResultRecordMap.get(documentName);
            for (CCTSResultRecord rr : resultRecords) {
                msg = msg + "+ State Name: " + rr.getDelivery().getStateName() + System.lineSeparator();
                msg = msg + "    + Provider: " + rr.getDelivery().getProvider() + System.lineSeparator();
                msg = msg + "    + Consumer: " + rr.getDelivery().getConsumer() + System.lineSeparator();
                msg = msg + "    + TestCaseId: " + rr.getDelivery().getTestCaseId() + System.lineSeparator();
                if(!isPassed){
                    // if failed, show the error message
                    msg = msg + "    + Failure message: " + rr.getErrorCode().getMessage() + System.lineSeparator();

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
                        result.getErrorCode().getMessage()
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
                        result.getErrorCode().getMessage()
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

    public Map<String, CCTSStatusCode> getContractVerificationResults() {
        return contractVerificationResults;
    }

    public void setContractVerificationResults(Map<String, CCTSStatusCode> contractVerificationResults) {
        this.contractVerificationResults = contractVerificationResults;
    }

    public CCTSStatusCode getVerifiedResult() {
        return verifiedResult;
    }

    public void setVerifiedResult(CCTSStatusCode verifiedResult) {
        this.verifiedResult = verifiedResult;
    }

    public Map<String, CCTSStatusCode> getCaseSequenceResults() {
        return caseSequenceResults;
    }

    public void setCaseSequenceResults(Map<String, CCTSStatusCode> caseSequenceResults) {
        this.caseSequenceResults = caseSequenceResults;
    }
}
