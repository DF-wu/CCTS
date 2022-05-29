package tw.dfder.ccts.services.reportexporter;

import tw.dfder.ccts.entity.cctsresultmodel.CCTSDocumentError;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTestStage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PreDocumentLevelForm implements ReportExportEngine{
    private static final String lineSeparator = "\n";
    @Override
    public String exportReport(CCTSTest cctsTest) {
        String msg = "";
        msg += "# CCTS TEST REPORT" + lineSeparator;
        msg += "# Information:" + lineSeparator;
        // must fail for this form
        msg += "+ Test result: " + "Failed" + lineSeparator;
        msg += "+ Test time: " + LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + lineSeparator;


        // show test stage instruction
        msg += "# Test Stage Instruction: " + lineSeparator;
        msg += "1. " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.DOCUMENT_STAGE.getInstruction() ;
        msg += "2. " + CCTSTestStage.PATH_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.PATH_STAGE.getInstruction() ;
        msg += "3. " + CCTSTestStage.CONTRACT_RETRIEVAL_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.CONTRACT_RETRIEVAL_STAGE.getInstruction() ;
        msg += "4. " + CCTSTestStage.CONTRACT_TEST_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.CONTRACT_TEST_STAGE.getInstruction() ;
        msg += "5. " + CCTSTestStage.EVENTLOG_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.EVENTLOG_STAGE.getInstruction();
        msg += "6. " + CCTSTestStage.PATH_VERIFY_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.PATH_VERIFY_STAGE.getInstruction() ;



        msg += "# Integration test Results: " + lineSeparator;

        msg += "## " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
        msg += "+ Test result: Failed" + lineSeparator;
        // show if title is duplicated.
        if(cctsTest.isDuplicatedTitle()) {
            msg += "+ Duplicated title occurred. Please correct CCTS Document." + lineSeparator;
        }

        for (CCTSDocumentError error : cctsTest.getDocumentErrors()) {
            msg += "### CCTS Document File Name: " + error.getDocument_file_name() + lineSeparator;
            msg += "    + Error Message: " + error.getStatusCode().getMessage() + lineSeparator;
            msg += "    " + error.getError() + lineSeparator;
            msg += error.getError() + lineSeparator;
        }


        return msg;
    }
}
