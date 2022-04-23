package tw.dfder.ccts.entity.cctsresultmodel;

import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;

public class CCTSResultRecord {
    private final String documentTitle;
    private final NextState delivery;
    private final CCTSStatusCode errorCode;

    public CCTSResultRecord(String documentTitle, NextState delivery, CCTSStatusCode errorCode) {
        this.documentTitle = documentTitle;
        this.delivery = delivery;
        this.errorCode = errorCode;
    }

//    below for accessor


    public String getDocumentTitle() {
        return documentTitle;
    }

    public NextState getDelivery() {
        return delivery;
    }

    public CCTSStatusCode getErrorCode() {
        return errorCode;
    }
}
