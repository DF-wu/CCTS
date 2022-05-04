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

    public CCTSTest(ArrayList<CCTSDocument> cctsDocuments) {
        this.results = new ArrayList<>();
        for ( CCTSDocument cctsDocument : cctsDocuments ) {
            results.add(new CCTSResult(cctsDocument));
        }
    }

    public boolean checkOut() {
        boolean isAllPassed = true;
        for ( CCTSResult result : results ) {
            if ( result.isTestResult() ) {
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

}
