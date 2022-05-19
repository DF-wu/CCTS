package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.repository.CCTSTestRepository;
import tw.dfder.ccts.services.reportexporter.CCTSVerifyLevelForm;
import tw.dfder.ccts.services.reportexporter.DocumentLevelForm;
import tw.dfder.ccts.services.reportexporter.PreDocumentLevelForm;
import tw.dfder.ccts.services.reportexporter.ReportExportEngine;

@Component
public class SystemStarter {
    public static boolean isSystemReady = false;
    private final ServiceConfigure serviceConfig;
    private final CCTSDocumentParser parser;
    private final CCTSVerifier cctsVerifier;
    private final DBCleaner cleaner;
    private final DocumentVerifier documentVerifier;
    private final CCTSTestRepository testRepository;


    @Autowired
    public SystemStarter(ServiceConfigure serviceConfig, CCTSDocumentParser parser, CCTSVerifier cctsVerifier, DBCleaner cleaner, DocumentVerifier documentVerifier, CCTSTestRepository testRepository) {
        this.serviceConfig = serviceConfig;
        this.parser = parser;
        this.cctsVerifier = cctsVerifier;
        this.cleaner = cleaner;
        this.documentVerifier = documentVerifier;
        this.testRepository = testRepository;
    }


    public void systemInit(){

        System.out.println(
                " .oooooo..o                          .                                    ooooooooo.                             .o8              .o. \n" +
                "d8P'    `Y8                        .o8                                    `888   `Y88.                          \"888              888 \n" +
                "Y88bo.      oooo    ooo  .oooo.o .o888oo  .ooooo.  ooo. .oo.  .oo.         888   .d88'  .ooooo.   .oooo.    .oooo888  oooo    ooo 888 \n" +
                " `\"Y8888o.   `88.  .8'  d88(  \"8   888   d88' `88b `888P\"Y88bP\"Y88b        888ooo88P'  d88' `88b `P  )88b  d88' `888   `88.  .8'  Y8P \n" +
                "     `\"Y88b   `88..8'   `\"Y88b.    888   888ooo888  888   888   888        888`88b.    888ooo888  .oP\"888  888   888    `88..8'   `8' \n" +
                "oo     .d8P    `888'    o.  )88b   888 . 888    .o  888   888   888        888  `88b.  888    .o d8(  888  888   888     `888'    .o. \n" +
                "8\"\"88888P'      .8'     8\"\"888P'   \"888\" `Y8bod8P' o888o o888o o888o      o888o  o888o `Y8bod8P' `Y888\"\"8o `Y8bod88P\"     .8'     Y8P \n" +
                "            .o..P'                                                                                                    .o..P'          \n" +
                "            `Y8P'                                                                                                     `Y8P'           \n" +
                "                                                                                                                                      ");
        isSystemReady = true;
    }

    public String startCCTSTest(){

        // clean ccts db
        cleaner.cleanCCTSDocumentDB();

        String msg = "";
        if(isSystemReady){
            CCTSTest cctsTest = new CCTSTest();
            // document prepare stage
            CCTSStatusCode prepareDocumentResult = documentVerifier.prepareDocumentVerify(cctsTest);
            if(prepareDocumentResult != CCTSStatusCode.ALLGREEN){
                // title duplicated or snake yaml exception
                ReportExportEngine reportExportEngine = new PreDocumentLevelForm();
                return reportExportEngine.exportReport(cctsTest);
            }

            // CCTS Document Verification Stage and Path Construction and Verification stage
            documentVerifier.verifyDirector(cctsTest);

            for ( CCTSResult result : cctsTest.getResults()) {
                // if previous test is not pass, skip this test
                if(result.getTestProgress().get(0).isTestResult() && result.getTestProgress().get(1).isTestResult()){
                    cctsVerifier.verifyCCTSDelivery(result);
                }else{
                    continue;
                }
            }
            cctsTest.checkOut();

            ReportExportEngine reportExportEngine = new CCTSVerifyLevelForm();
            msg = reportExportEngine.exportReport(cctsTest);
            testRepository.save(cctsTest);
            return msg;

        }else{
            return "System is not ready. Try again later.";
        }

    }


}
