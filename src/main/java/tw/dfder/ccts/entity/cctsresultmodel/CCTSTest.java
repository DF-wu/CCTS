package tw.dfder.ccts.entity.cctsresultmodel;


import org.springframework.data.mongodb.core.mapping.Document;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "CCTSTest")
public class CCTSTest {
    private boolean cctsTestResult = false;
    private ArrayList<CCTSResult> results;

    private ArrayList<CCTSDocumentError> documentErrors;

    private boolean isDuplicatedTitle = false;

    public CCTSTest() {
        results = new ArrayList<>();
        documentErrors = new ArrayList<>();
    }

    public void addDocuments(ArrayList<CCTSDocument> documents) {
        for ( CCTSDocument document : documents ) {
            this.results.add(new CCTSResult(document));
        }
    }

//    public CCTSTest(ArrayList<CCTSDocument> cctsDocuments) {
//        this.results = new ArrayList<>();
//        this.documentErrors = new ArrayList<>();
//        for ( CCTSDocument cctsDocument : cctsDocuments ) {
//            results.add(new CCTSResult(cctsDocument));
//        }
//
//    }


    public void addDocumentError(CCTSDocumentError documentError) {
        documentErrors.add(documentError);
    }
    public boolean checkOut() {
        boolean isAllPassed = true;
        for ( CCTSResult result : results ) {

            if ( result.checkOut() ) {
                // pass
            }else {
                // any fail
                isAllPassed = false;
            }
        }
        cctsTestResult = isAllPassed;
        return cctsTestResult;
    }



    public ArrayList<CCTSResult> getResults() {
        return results;
    }

    public void setResults(ArrayList<CCTSResult> results) {
        this.results = results;
    }

    public boolean isCctsTestResult() {
        return cctsTestResult;
    }

    public ArrayList<CCTSDocumentError> getDocumentErrors() {
        return documentErrors;
    }

    public void setDocumentErrors(ArrayList<CCTSDocumentError> documentErrors) {
        this.documentErrors = documentErrors;
    }

    public boolean isDuplicatedTitle() {
        return isDuplicatedTitle;
    }

    public void setDuplicatedTitle(boolean duplicatedTitle) {
        isDuplicatedTitle = duplicatedTitle;
    }
}
