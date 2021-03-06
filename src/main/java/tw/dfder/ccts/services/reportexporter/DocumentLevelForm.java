package tw.dfder.ccts.services.reportexporter;

import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTestStage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DocumentLevelForm implements ReportExportEngine{
    private static final String lineSeparator = "\n";
    @Override
    public String exportReport(CCTSTest cctsTest) {
        String msg = "# CCTS Document Parse Stage Report\n";
        msg += "## Information" + lineSeparator;
        // for this exporter enging , muse be fail.
        msg += "+ Test result: Failed." + lineSeparator;
        msg += "+ Test Time: " + LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + lineSeparator;

        // show test stage instruction
        msg += "## Test Stage Instruction: " + lineSeparator;
        msg += "    1. " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.DOCUMENT_STAGE.getInstruction() ;
        msg += "    2. " + CCTSTestStage.PATH_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.PATH_STAGE.getInstruction();
        msg += "    3. " + CCTSTestStage.CONTRACT_RETRIEVAL_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.CONTRACT_RETRIEVAL_STAGE.getInstruction() ;
        msg += "    4. " + CCTSTestStage.CONTRACT_TEST_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.CONTRACT_TEST_STAGE.getInstruction() ;
        msg += "    5. " + CCTSTestStage.EVENTLOG_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.EVENTLOG_STAGE.getInstruction() ;
        msg += "    6. " + CCTSTestStage.PATH_VERIFY_STAGE.getStageName() + lineSeparator;
        msg +=  CCTSTestStage.PATH_VERIFY_STAGE.getInstruction() ;

        // document level error
        msg += "## Integration test Results: " + lineSeparator;

        boolean isDocPass = true;
        for (CCTSResult result  : cctsTest.getResults()) {
            if(result.getDocumentStageVerificationErrors().size() > 0){
                isDocPass = false;
            }
        }

        if(!isDocPass){
            msg += "### " + CCTSTestStage.DOCUMENT_STAGE.getStageName() + lineSeparator;
            for (CCTSResult result : cctsTest.getResults()) {
                // output error message
                if(result.getDocumentStageVerificationErrors().size() != 0) {
                    // document name
                    msg += "#### CCTS Document Name: " + result.getDocument().getTitle()+ lineSeparator;
                    msg += "+ Test result: Failed" + lineSeparator;
                    // document legality error
                    for ( CCTSStatusCode code : result.getDocumentStageVerificationErrors() ) {
                        msg += "+ Failure description: " + code.getMessage() + lineSeparator;
                    }
                }
            }
        }else {
            msg += "+ Test result: Passed" + lineSeparator;
        }




        // path level error

        boolean isPathPass =  true;
        for ( CCTSResult result : cctsTest.getResults() ) {
             if ( result.getPathConstructionAndVerificationErrors().size() != 0 ) {
                 isPathPass = false;
             }
        }

        msg += "### " + CCTSTestStage.PATH_STAGE.getStageName() + lineSeparator;
        if(!isPathPass){
            for (CCTSResult result : cctsTest.getResults()) {
                // output error message
                if(result.getPathConstructionAndVerificationErrors().size() != 0) {
                    // document name
                    msg += "#### CCTS Document Name: " + result.getDocument().getTitle()+ lineSeparator;
                    msg += "+ Test result: Failed" + lineSeparator;
                    // document error
                    for ( CCTSStatusCode code : result.getPathConstructionAndVerificationErrors() ) {
                        msg += "+ Failure description: " + code.getMessage() + lineSeparator;
                    }
                }
            }
        }else {
            msg += "+ Test result: Passed" + lineSeparator;
        }



        return msg;
    }
}
