package tw.dfder.ccts.services.reportexporter;

import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResultRecord;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTestStage;
import tw.dfder.ccts.services.CCTSDocumentParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CCTSVerifyLevelForm implements ReportExportEngine {

    private static final String lineSeparator = "\n";
    /*
    # CCTS TEST REPORT
    ## Information
    + Test result
    + Test time:
    ## Documents test Results
    ### document Name : $name
    + Delivery Number:
    + Potential Path number:
    + Participant Service:
      + service 1
      + service 2
    ### Test Stage:
    1. CCTS test Stage name :
      + result : result
      + stage id:
      + test instruction
        + ....
    2. ...
    ### Result Detail:
    #### Potential Path
    ##### Pass
    + path
    ##### Fail
    + path : reason
    #### Eventlogs Stage
    ##### pass
    + Eventlogs test caseID
      + provider
      + consumer
      + timeSequentialLabel
    ##### fail
    + Eventlogs test caseID
      + provider
      + consumer
      + timeSequentialLabel
      + Fail reason
    #### Path Verify Stage
    ##### PASS
    + path:
    ##### FAIL
    + path:
      + fail reason:
    #### Contract Stage
    ##### PASS
    + Eventlogs test caseID
      + provider
      + consumer
      + timeSequentialLabel
    ##### FAIL
    + Eventlogs test caseID
      + provider
      + consumer
      + timeSequentialLabel
      + Fail reason:
    #### Contract Test Verification
    ##### pass
    + service
    ##### fail
    + service
      + fail reason
     */


    @Override
    public String exportReport(CCTSTest cctsTest) {

        String msg = "";
        msg += "# CCTS TEST REPORT" + lineSeparator;
        msg += "# Information:" + lineSeparator;
        msg += "+ Test result: " + (cctsTest.isCctsTestResult() ? "Passed." : "Failed.") + lineSeparator;
        msg += "+ Test time: " + LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + lineSeparator;

        msg += "# Test Stage Instructions: " + lineSeparator;
        msg += "1. " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.DOCUMENT_STAGE.getInstruction() + lineSeparator;
        msg += "2. " + CCTSTestStage.PATH_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.PATH_STAGE.getInstruction() + lineSeparator;
        msg += "3. " + CCTSTestStage.CONTRACT_RETRIEVAL_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.CONTRACT_RETRIEVAL_STAGE.getInstruction() + lineSeparator;
        msg += "4. " + CCTSTestStage.CONTRACT_TEST_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.CONTRACT_TEST_STAGE.getInstruction() + lineSeparator;
        msg += "5. " + CCTSTestStage.EVENTLOG_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.EVENTLOG_STAGE.getInstruction() + lineSeparator;
        msg += "6. " + CCTSTestStage.PATH_VERIFY_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.PATH_VERIFY_STAGE.getInstruction() + lineSeparator;


        msg += "# Integration Test Results:" + lineSeparator;
        for (CCTSResult cctsResult : cctsTest.getResults()) {

            ArrayList<ArrayList<NextState>> paths = new ArrayList<>();
            // possible branch : 00 10 11
            if(cctsResult.getTestProgress().get(0).isTestResult() && cctsResult.getTestProgress().get(1).isTestResult()){
                // 11
                // document legality pass and pass path test
                msg += "## Integration Test Case Name:" + cctsResult.getDocument().getTitle() + lineSeparator;
                msg += "+ Participant Service: " + lineSeparator;
                for (String service : CCTSDocumentParser.findAllParticipants(cctsResult.getDocument())) {
                    msg += "    + " + service + lineSeparator;
                }
                msg += "+ Number of Message Deliveries: " + CCTSDocumentParser.findDeliveryList(cctsResult.getDocument()).size() + lineSeparator;
                CCTSDocumentParser.pathFinder(cctsResult.getDocument(), cctsResult.getDocument().findSimpleState(cctsResult.getDocument().getStartAt()), new ArrayList<>(), paths);
                msg += "+ Number of Potential Paths: " + paths.size() + lineSeparator;
                // add potential path
                for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
                    msg += "    + " + pathName + lineSeparator;
                }

                msg += "### Test Result Details: " + lineSeparator;

                //must pass
                msg += "#### " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
                msg += "+ Test Result: Passed." + lineSeparator;

                msg += "#### "+ CCTSTestStage.PATH_STAGE.getStageName() + lineSeparator;
                msg += "+ Test Result: Passed." + lineSeparator;



                msg += "#### " + CCTSTestStage.CONTRACT_RETRIEVAL_STAGE.getStageName() + lineSeparator;
                msg = generateContractVerificationEntityMD(msg, cctsResult);

                msg += "#### " + CCTSTestStage.CONTRACT_TEST_STAGE.getStageName() + lineSeparator;
                msg = gernerateContractTestResultEntutyMD(msg, cctsResult);

                msg += "#### " + CCTSTestStage.EVENTLOG_STAGE.getStageName() + lineSeparator;
                msg = generateEventLogsVerificationEntityMD(msg, cctsResult);

                msg += "#### " + CCTSTestStage.PATH_VERIFY_STAGE.getStageName() + lineSeparator;
                msg = generatePotentialPathVerificationEntityMD(msg, cctsResult);



            }else if(cctsResult.getTestProgress().get(0).isTestResult() && !cctsResult.getTestProgress().get(1).isTestResult()){
                // 10
                // document legality pass but not pass path test
                msg += "## Integration Test Case Name:" + cctsResult.getDocument().getTitle() + lineSeparator;
                msg += "+ Participant Service: " + lineSeparator;
                for (String service : CCTSDocumentParser.findAllParticipants(cctsResult.getDocument())) {
                    msg += "    + " + service + lineSeparator;
                }
                msg += "+ Number of Message Deliveries: " + CCTSDocumentParser.findDeliveryList(cctsResult.getDocument()).size() + lineSeparator;
                CCTSDocumentParser.pathFinder(cctsResult.getDocument(), cctsResult.getDocument().findSimpleState(cctsResult.getDocument().getStartAt()), new ArrayList<>(), paths);

                msg += "### Test Result Details: " + lineSeparator;

                //must pass
                msg += "#### " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
                msg += "+ Test Result: Passed." + lineSeparator;

                msg += "#### "+ CCTSTestStage.PATH_STAGE.getStageName() + lineSeparator;
                msg += "+ Test Result: Failed" + lineSeparator;


                // output error message
                if(cctsResult.getPathConstructionAndVerificationErrors().size() != 0) {
                    // document error
                    msg += "+ Failure description: " + lineSeparator;
                    for ( CCTSStatusCode code : cctsResult.getPathConstructionAndVerificationErrors() ) {
                        msg += "    + " + code.getMessage() + lineSeparator;

                    }
                }



            }else {
                // 00
                msg += "## Integration Test Case Name:" + cctsResult.getDocument().getTitle() + lineSeparator;
                msg += "### Test Result Details: " + lineSeparator;
                msg += "#### " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
                msg += "+ Test Result: Failed." + lineSeparator;
                // output error message
                if(cctsResult.getDocumentStageVerificationErrors().size() != 0) {
                    // document legality error
                    msg += "+ Failure description: " + lineSeparator;
                    for ( CCTSStatusCode code : cctsResult.getDocumentStageVerificationErrors() ) {
                        msg += "    + " + code.getMessage() + lineSeparator;
                    }
                }




            }


//            msg += "### Test Stage Instruction: " + lineSeparator;
//            for (int i = 0; i < cctsResult.getTestProgress().size(); i++) {
//                msg += (i + 1) + ". " + cctsResult.getTestProgress().get(i).getTestStages().getStageName() + lineSeparator;
//                msg += "    + test instruction: " + lineSeparator;
//                msg += cctsResult.getTestProgress().get(i).getTestStages().getInstruction()+ lineSeparator;
//            }

        }


        return msg;
    }

    private String gernerateContractTestResultEntutyMD(String msg, CCTSResult cctsResult) {
        boolean testResult = true;
        for (CCTSStatusCode code: cctsResult.getContractVerificationResults().values()) {
            if(code != CCTSStatusCode.ALLGREEN){
                testResult = false;
            }
        }

        if (testResult){
            msg += "+ Test Result: Passed." + lineSeparator;
        }else {
            msg += "+ Test Result: Failed." + lineSeparator;
        }





        msg += "##### Pass" + lineSeparator;
        boolean noPassResult = true;
        for (CCTSStatusCode code: cctsResult.getContractVerificationResults().values()) {
            if(code == CCTSStatusCode.ALLGREEN) {
                noPassResult = false;
            }
        }
        if (noPassResult) {
            msg += "None" + lineSeparator;
        }else {
            for (String service: cctsResult.getContractVerificationResults().keySet()) {
                if (cctsResult.getContractVerificationResults().get(service) == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + service + lineSeparator;
                }
            }

        }

        msg += "##### Fail" + lineSeparator;
        boolean noInvalid = true;
        for (String service: cctsResult.getContractVerificationResults().keySet()) {
            if (cctsResult.getContractVerificationResults().get(service) != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }
        if (noInvalid) {
            msg += "None" + lineSeparator;
        }else {
            for (String service: cctsResult.getContractVerificationResults().keySet()) {
                if (cctsResult.getContractVerificationResults().get(service) != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + service + lineSeparator;
                    msg += "    + Failure description: " + cctsResult.getContractVerificationResults().get(service).getMessage() + lineSeparator;
                }
            }
        }
        return msg;
    }

    private String generateContractVerificationEntityMD(String msg, CCTSResult cctsResult) {
        boolean testResult = true;
        for (CCTSResultRecord rr: cctsResult.getResultBetweenDeliveryAndContract()) {
            if(rr.getResultCode() != CCTSStatusCode.ALLGREEN){
                testResult = false;
            }
        }

        if (testResult){
            msg += "+ Test Result: Passed." + lineSeparator;
        }else {
            msg += "+ Test Result: Failed." + lineSeparator;
        }





        msg += "##### Pass" + lineSeparator;
        boolean noPassResult = true;
        for (CCTSResultRecord rr : cctsResult.getResultBetweenDeliveryAndContract()) {
            if (rr.getResultCode() == CCTSStatusCode.ALLGREEN) {
                noPassResult = false;
            }
        }

        if (noPassResult) {
            msg += "None" + lineSeparator;
        } else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndContract()) {
                if (resultRecord.getResultCode() == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + lineSeparator;
                    msg += "    + Provider: " + resultRecord.getDelivery().getProvider() + lineSeparator;
                    msg += "    + Consumer: " + resultRecord.getDelivery().getConsumer() + lineSeparator;
                    //msg += "    + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + lineSeparator;

                }
            }

        }


        msg += "##### Fail" + lineSeparator;
        boolean noInvalid = true;
        for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndContract()) {
            if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }
        if (noInvalid) {
            msg += "None" + lineSeparator;
        }else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndContract()) {
                if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + lineSeparator;
                    msg += "    + Provider: " + resultRecord.getDelivery().getProvider() + lineSeparator;
                    msg += "    + Consumer: " + resultRecord.getDelivery().getConsumer() + lineSeparator;
                    //msg += "    + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + lineSeparator;
                    msg += "    + Failure description: " + resultRecord.getResultCode().getMessage() + lineSeparator;

                }
            }
        }
        return msg;
    }

    private String generateEventLogsVerificationEntityMD(String msg, CCTSResult cctsResult) {
        boolean testResult = true;
        for (CCTSResultRecord rr: cctsResult.getResultBetweenDeliveryAndEventLogs()) {
            if(rr.getResultCode() != CCTSStatusCode.ALLGREEN){
                testResult = false;
            }
        }

        if (testResult){
            msg += "+ Test Result: Passed." + lineSeparator;
        }else {
            msg += "+ Test Result: Failed." + lineSeparator;
        }




        msg += "##### Pass" + lineSeparator;
        boolean isPassEmpty = false;
        for (CCTSResultRecord rr: cctsResult.getResultBetweenDeliveryAndEventLogs()) {
            if (rr.getResultCode() == CCTSStatusCode.ALLGREEN) {
                isPassEmpty = true;
            }
        }
        if (!isPassEmpty) {
            msg += "None" + lineSeparator;
        }else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndEventLogs()) {
                if (resultRecord.getResultCode() == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + lineSeparator;
                    msg += "    + Provider: " + resultRecord.getDelivery().getProvider() + lineSeparator;
                    msg += "    + Consumer: " + resultRecord.getDelivery().getConsumer() + lineSeparator;
                    //msg += "    + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + lineSeparator;
                }
            }
        }

        msg += "##### Fail" + lineSeparator;
        boolean noInvalid = true;
        for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndEventLogs()) {
            if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }

        if(noInvalid) {
            msg += "None" + lineSeparator;
        }else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndEventLogs()) {
                if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + lineSeparator;
                    msg += "    + Provider: " + resultRecord.getDelivery().getProvider() + lineSeparator;
                    msg += "    + Consumer: " + resultRecord.getDelivery().getConsumer() + lineSeparator;
                    //msg += "    + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + lineSeparator;
                    msg += "    + Failure description: " + resultRecord.getResultCode().getMessage() + lineSeparator;

                }
            }
        }
        return msg;
    }

    private String generatePotentialPathVerificationEntityMD(String msg, CCTSResult cctsResult) {
        boolean testResult = true;
        for (CCTSStatusCode code: cctsResult.getPathVerificationResults().values()) {
            if(code != CCTSStatusCode.ALLGREEN){
                testResult = false;
            }
        }

        if (testResult){
            msg += "+ Test Result: Passed." + lineSeparator;
        }else {
            msg += "+ Test Result: Failed." + lineSeparator;
        }


        msg += "##### Pass" + lineSeparator;
        boolean passIsNotEmpty = false;
        for (CCTSStatusCode code : cctsResult.getPathVerificationResults().values()) {
            if(code == CCTSStatusCode.ALLGREEN) {
                passIsNotEmpty = true;
            }
        }

        if (passIsNotEmpty) {
            for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
                if (cctsResult.getPathVerificationResults().get(pathName) == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + pathName + lineSeparator;
                }
            }
        }else {
            msg += "None" + lineSeparator;

        }

        msg += "##### Fail" + lineSeparator;
        // if no fail add none word
        boolean noInvalid = true;
        for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
            if(cctsResult.getPathVerificationResults().get(pathName) != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }
        if(noInvalid) {
            msg += "None" + lineSeparator;
        }else {
            for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
                if (cctsResult.getPathVerificationResults().get(pathName) != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + pathName + lineSeparator;
                    msg += "    + Failure description: " + cctsResult.getPathVerificationResults().get(pathName).getMessage() + lineSeparator;
                }
            }
        }
        return msg;
    }
}
