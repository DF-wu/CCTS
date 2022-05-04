package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTestCase;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTestStage;
import tw.dfder.ccts.repository.CCTSTestRepository;
import tw.dfder.ccts.services.reportexporter.DocumentLevelForm;
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
            // document prepare stage
            // A-1
            CCTSStatusCode prepareDocumentResult = documentVerifier.prepareDocumentVerify();
            if(prepareDocumentResult == CCTSStatusCode.DOCUMENT_DUPLICATED_STATE_NAME_ERROR){
                return CCTSStatusCode.DOCUMENT_DUPLICATED_STATE_NAME_ERROR.getMessage();
            }else if(prepareDocumentResult == CCTSStatusCode.CCTSDOCUMENT_PARSE_ERROR){
                // maybe snake yaml exception
                String exmsg = documentVerifier.prepareDocumentErrorMessage;
                exmsg = "CCTSDocument parse exception: \n" + exmsg;
                return exmsg;
            }

            // document stage
            // A-2
            CCTSTest cctsTest = documentVerifier.verifyDirector();
            boolean isDocumentValid = true;
            for (CCTSResult result : cctsTest.getResults()) {



                if(result.getDocumentStageError() != CCTSStatusCode.ALLGREEN){
                    // A-2 fail

                    isDocumentValid = false;
                }else{
                    // A-2 pass


                }
            }
            // if any error occur, return error message
            if(isDocumentValid){
                ReportExportEngine reportExportEngine = new DocumentLevelForm();
                return reportExportEngine.exportReport(cctsTest);
            }


            //
            for ( CCTSResult result : cctsTest.getResults()) {
                //TODO return format output
                cctsVerifier.verifyCCTSDelivery(result);
            }



            cctsTest.checkOut();
            testRepository.save(cctsTest);
            return msg;

        }else{
            return "System is not ready";
        }

    }


}
