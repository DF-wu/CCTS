package tw.dfder.ccts.entity.cctsresultmodel;

import tw.dfder.ccts.entity.CCTSStatusCode;

import java.util.ArrayList;

public class CCTSTestCase {

    private CCTSTestStage testSubject;
    private boolean testResult;


    public CCTSTestCase(CCTSTestStage testSubject, Boolean testResult) {
        this.testSubject = testSubject;
        this.testResult = testResult;
    }




    public CCTSTestStage getTestStages() {
        return testSubject;
    }

    public void setTestSubject(CCTSTestStage testSubject) {
        this.testSubject = testSubject;
    }



    public void setTestResult(Boolean testResult) {
        this.testResult = testResult;
    }

    public boolean isTestResult() {
        return testResult;
    }


}
