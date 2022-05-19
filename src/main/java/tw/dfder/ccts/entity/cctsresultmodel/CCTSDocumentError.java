package tw.dfder.ccts.entity.cctsresultmodel;

import tw.dfder.ccts.entity.CCTSStatusCode;

public class CCTSDocumentError {
    private String document_file_name;
    private CCTSStatusCode StatusCode;
    private String error;

    public CCTSDocumentError(String document_file_name, CCTSStatusCode statusCode, String error) {
        this.document_file_name = document_file_name;
        StatusCode = statusCode;
        this.error = error;
    }

    public String getDocument_file_name() {
        return document_file_name;
    }

    public void setDocument_file_name(String document_file_name) {
        this.document_file_name = document_file_name;
    }

    public CCTSStatusCode getStatusCode() {
        return StatusCode;
    }

    public void setStatusCode(CCTSStatusCode statusCode) {
        StatusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
