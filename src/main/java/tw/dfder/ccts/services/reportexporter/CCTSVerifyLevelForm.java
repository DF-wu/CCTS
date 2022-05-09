package tw.dfder.ccts.services.reportexporter;

import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResultRecord;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.services.CCTSDocumentParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CCTSVerifyLevelForm implements ReportExportEngine {

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
        msg += "# CCTS TEST REPORT" + System.lineSeparator();
        msg += "## Information:" + System.lineSeparator();
        msg += "+ Test result: " + cctsTest.isCctsTestResult() + System.lineSeparator();
        msg += "+ Test time: " + LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + System.lineSeparator();
        msg += "## Documents test Results" + System.lineSeparator();
        for (CCTSResult cctsResult : cctsTest.getResults()) {
            msg += "### document Name : " + cctsResult.getDocument().getTitle() + System.lineSeparator();
            msg += "+ Delivery Number: " + CCTSDocumentParser.findDeliveryList(cctsResult.getDocument()).size() + System.lineSeparator();
            ArrayList<ArrayList<NextState>> paths = new ArrayList<>();
            CCTSDocumentParser.pathFinder(cctsResult.getDocument(), cctsResult.getDocument().findSimpleState(cctsResult.getDocument().getStartAt()), new ArrayList<>(), paths);
            msg += "+ Potential Path number: " + paths.size() + System.lineSeparator();
            // add potential path
            for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
                msg += "  + " + pathName + System.lineSeparator();
            }
            msg += "+ Participant Service: " + System.lineSeparator();
            for (String service : CCTSDocumentParser.findAllParticipants(cctsResult.getDocument())) {
                msg += "  + " + service + System.lineSeparator();
            }
            msg += "### Test Stage: " + System.lineSeparator();
            for (int i = 0; i < cctsResult.getTestProgress().size(); i++) {
                msg += (i + 1) + ". " + cctsResult.getTestProgress().get(i).getTestStages().getStageName() + System.lineSeparator();
                msg += "  + result : " + cctsResult.getTestProgress().get(i).isTestResult() + System.lineSeparator();
                msg += "  + stage id: " + cctsResult.getTestProgress().get(i).getTestStages().getStageId() + System.lineSeparator();
                msg += "  + test instruction: " + System.lineSeparator();
                msg += "  " + cctsResult.getTestProgress().get(i).getTestStages().getInstruction()+ System.lineSeparator();
            }
            msg += "### Result Detail: " + System.lineSeparator();
            msg += "#### Potential Path" + System.lineSeparator();
            msg = generatePotentialPathVerificationEntityMD(msg, cctsResult);


            msg += "#### Eventlogs" + System.lineSeparator();
            msg = generateEventLogsVerificationEntityMD(msg, cctsResult);
            boolean noInvalid;
            msg += "#### Contract Stage" + System.lineSeparator();
            msg = generateContractVerificationEntityMD(msg, cctsResult);

            msg += "#### Contract Test Verification" + System.lineSeparator();
            msg = gernerateContractTestResultEntutyMD(msg, cctsResult);


        }


        return msg;
    }

    private String gernerateContractTestResultEntutyMD(String msg, CCTSResult cctsResult) {

        msg += "##### Pass" + System.lineSeparator();
        boolean noPassResult = true;
        for (CCTSStatusCode code: cctsResult.getContractVerificationResults().values()) {
            if(code == CCTSStatusCode.ALLGREEN) {
                noPassResult = false;
            }
        }
        if (!noPassResult) {
            msg += "None" + System.lineSeparator();
        }else {
            for (String service: cctsResult.getContractVerificationResults().keySet()) {
                if (cctsResult.getContractVerificationResults().get(service) == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + service + System.lineSeparator();
                }
            }

        }

        msg += "##### Fail" + System.lineSeparator();
        boolean noInvalid = true;
        for (String service: cctsResult.getContractVerificationResults().keySet()) {
            if (cctsResult.getContractVerificationResults().get(service) != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }
        if (noInvalid) {
            msg += "None" + System.lineSeparator();
        }else {
            for (String service: cctsResult.getContractVerificationResults().keySet()) {
                if (cctsResult.getContractVerificationResults().get(service) != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + service + System.lineSeparator();
                    msg += "  + fail reason: " + cctsResult.getContractVerificationResults().get(service).getMessage() + System.lineSeparator();
                }
            }
        }
        return msg;
    }

    private String generateContractVerificationEntityMD(String msg, CCTSResult cctsResult) {

        msg += "##### Pass" + System.lineSeparator();
        boolean noPassResult = true;
        for (CCTSResultRecord rr : cctsResult.getResultBetweenDeliveryAndContract()) {
            if (rr.getResultCode() == CCTSStatusCode.ALLGREEN) {
                noPassResult = false;
            }
        }

        if (noPassResult) {
            msg += "None" + System.lineSeparator();
        } else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndContract()) {
                if (resultRecord.getResultCode() == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + System.lineSeparator();
                    msg += "  + Provider: " + resultRecord.getDelivery().getProvider() + System.lineSeparator();
                    msg += "  + Consumer: " + resultRecord.getDelivery().getConsumer() + System.lineSeparator();
                    msg += "  + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + System.lineSeparator();

                }
            }

        }


        msg += "##### Fail" + System.lineSeparator();
        boolean noInvalid = true;
        for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndContract()) {
            if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }
        if (noInvalid) {
            msg += "None" + System.lineSeparator();
        }else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndContract()) {
                if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + System.lineSeparator();
                    msg += "  + Provider: " + resultRecord.getDelivery().getProvider() + System.lineSeparator();
                    msg += "  + Consumer: " + resultRecord.getDelivery().getConsumer() + System.lineSeparator();
                    msg += "  + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + System.lineSeparator();
                    msg += "  + fail reason: " + resultRecord.getResultCode().getMessage() + System.lineSeparator();

                }
            }
        }
        return msg;
    }

    private String generateEventLogsVerificationEntityMD(String msg, CCTSResult cctsResult) {
        msg += "##### Pass" + System.lineSeparator();
        boolean isPassEmpty = false;
        for (CCTSResultRecord rr: cctsResult.getResultBetweenDeliveryAndEventLogs()) {
            if (rr.getResultCode() == CCTSStatusCode.ALLGREEN) {
                isPassEmpty = true;
            }
        }
        if (!isPassEmpty) {
            msg += "None" + System.lineSeparator();
        }else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndEventLogs()) {
                if (resultRecord.getResultCode() == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + System.lineSeparator();
                    msg += "  + Provider: " + resultRecord.getDelivery().getProvider() + System.lineSeparator();
                    msg += "  + Consumer: " + resultRecord.getDelivery().getConsumer() + System.lineSeparator();
                    msg += "  + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + System.lineSeparator();
                }
            }
        }

        msg += "##### Fail" + System.lineSeparator();
        boolean noInvalid = true;
        for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndEventLogs()) {
            if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }

        if(noInvalid) {
            msg += "None" + System.lineSeparator();
        }else {
            for (CCTSResultRecord resultRecord : cctsResult.getResultBetweenDeliveryAndEventLogs()) {
                if (resultRecord.getResultCode() != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + resultRecord.getDelivery().getTestCaseId() + System.lineSeparator();
                    msg += "  + Provider: " + resultRecord.getDelivery().getProvider() + System.lineSeparator();
                    msg += "  + Consumer: " + resultRecord.getDelivery().getConsumer() + System.lineSeparator();
                    msg += "  + TimeSequenceLabel: " + resultRecord.getDelivery().getTimeSequenceLabel() + System.lineSeparator();
                    msg += "  + fail reason: " + resultRecord.getResultCode().getMessage() + System.lineSeparator();

                }
            }
        }
        return msg;
    }

    private String generatePotentialPathVerificationEntityMD(String msg, CCTSResult cctsResult) {
        msg += "##### Pass" + System.lineSeparator();
        boolean passIsNotEmpty = false;
        for (CCTSStatusCode code : cctsResult.getPathVerificationResults().values()) {
            if(code == CCTSStatusCode.ALLGREEN) {
                passIsNotEmpty = true;
            }
        }
        if (passIsNotEmpty) {
            for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
                if (cctsResult.getPathVerificationResults().get(pathName) == CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + pathName + System.lineSeparator();
                }
            }
        }else {
            msg += "None" + System.lineSeparator();

        }

        msg += "##### Fail" + System.lineSeparator();
        // if no fail add none word
        boolean noInvalid = true;
        for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
            if(cctsResult.getPathVerificationResults().get(pathName) != CCTSStatusCode.ALLGREEN) {
                noInvalid = false;
            }
        }
        if(noInvalid) {
            msg += "None" + System.lineSeparator();
        }else {
            for (String pathName : cctsResult.getPathVerificationResults().keySet() ) {
                if (cctsResult.getPathVerificationResults().get(pathName) != CCTSStatusCode.ALLGREEN) {
                    msg += "+ " + pathName + System.lineSeparator();
                    msg += "  + fail reason: " + cctsResult.getPathVerificationResults().get(pathName).getMessage() + System.lineSeparator();
                }
            }
        }
        return msg;
    }
}
