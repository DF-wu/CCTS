package tw.dfder.ccts.entity.cctsresultmodel;

import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;

public class ResultRecord {
    private final String documentTitle;
    private final NextState path;
    private final CCTSStatusCode errorCode;

    public ResultRecord(String documentTitle, NextState path, CCTSStatusCode errorCode) {
        this.documentTitle = documentTitle;
        this.path = path;
        this.errorCode = errorCode;
    }

//    below for accessor


    public String getDocumentTitle() {
        return documentTitle;
    }

    public NextState getPath() {
        return path;
    }

    public CCTSStatusCode getErrorCode() {
        return errorCode;
    }
}
