package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tw.dfder.ccts.configuration.ServiceConfigure;

@Component
public class SystemStarter {
    public static boolean isSystemReady = false;
    private final ServiceConfigure serviceConfig;
    private final CCTSDocumentParser parser;
    private final CCTSVerifier verifier;
    private final DBCleaner cleaner;

    @Autowired
    public SystemStarter(ServiceConfigure serviceConfig, CCTSDocumentParser parser, CCTSVerifier verifier, DBCleaner cleaner) {
        this.serviceConfig = serviceConfig;
        this.parser = parser;
        this.verifier = verifier;
        this.cleaner = cleaner;
    }


    public void systemInit(){
        // clean ccts db
        cleaner.cleanCCTSProfileDB();
        parser.parseAllCCTSProfilesAndSave2DB();
        isSystemReady = true;

        System.out.println(" .oooooo..o                          .                                    ooooooooo.                             .o8              .o. \n" +
                "d8P'    `Y8                        .o8                                    `888   `Y88.                          \"888              888 \n" +
                "Y88bo.      oooo    ooo  .oooo.o .o888oo  .ooooo.  ooo. .oo.  .oo.         888   .d88'  .ooooo.   .oooo.    .oooo888  oooo    ooo 888 \n" +
                " `\"Y8888o.   `88.  .8'  d88(  \"8   888   d88' `88b `888P\"Y88bP\"Y88b        888ooo88P'  d88' `88b `P  )88b  d88' `888   `88.  .8'  Y8P \n" +
                "     `\"Y88b   `88..8'   `\"Y88b.    888   888ooo888  888   888   888        888`88b.    888ooo888  .oP\"888  888   888    `88..8'   `8' \n" +
                "oo     .d8P    `888'    o.  )88b   888 . 888    .o  888   888   888        888  `88b.  888    .o d8(  888  888   888     `888'    .o. \n" +
                "8\"\"88888P'      .8'     8\"\"888P'   \"888\" `Y8bod8P' o888o o888o o888o      o888o  o888o `Y8bod8P' `Y888\"\"8o `Y8bod88P\"     .8'     Y8P \n" +
                "            .o..P'                                                                                                    .o..P'          \n" +
                "            `Y8P'                                                                                                     `Y8P'           \n" +
                "                                                                                                                                      ");
    }

    public boolean startCCTSTest(){
        if(isSystemReady){
            return verifier.verifyCCTSProfileSAGAFlow();
        }else{
            return false;
        }

    }


}
