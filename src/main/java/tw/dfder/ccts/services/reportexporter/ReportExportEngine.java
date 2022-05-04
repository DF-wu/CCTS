package tw.dfder.ccts.services.reportexporter;

import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;

public interface ReportExportEngine {

    public String exportReport(CCTSTest cctsTest);

}
