package tw.dfder.ccts.entity.cctsresultmodel;

import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;

public class ErrorRecord {
    private final String documentTitle;
    private final NextState path;
    private final CCTSStatusCode errorCode;

    public ErrorRecord(String documentTitle, NextState path, CCTSStatusCode errorCode) {
        this.documentTitle = documentTitle;
        this.path = path;
        this.errorCode = errorCode;
    }



}
