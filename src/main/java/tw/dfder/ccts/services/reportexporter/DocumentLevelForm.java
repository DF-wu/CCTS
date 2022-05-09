package tw.dfder.ccts.services.reportexporter;

import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DocumentLevelForm implements ReportExportEngine{

    @Override
    public String exportReport(CCTSTest cctsTest) {
        String msg = "# CCTS Document Parse Stage Report\n";
        msg += "## Information" + System.lineSeparator();
        msg += "+ Test Time: " + LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + System.lineSeparator();
        msg += "## Error List: " + System.lineSeparator();
        for (CCTSResult result : cctsTest.getResults()) {
            // output error message
            if(result.getDocumentStageVerificationError() != CCTSStatusCode.ALLGREEN) {
                // document name
                msg += "### " + result.getDocument().getTitle()+ System.lineSeparator();
                // document error
                msg += "+ Error Reason: " + result.getDocumentStageVerificationError().getMessage() + System.lineSeparator();

            }

        }

        return msg;
    }
}
