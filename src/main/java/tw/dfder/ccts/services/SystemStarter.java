package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tw.dfder.ccts.configuration.ServiceConfigure;

@Component
public class SystemStarter {
    public static boolean isSystemReady = false;
    private final ServiceConfigure serviceConfig;
    private final CCTSDocumentParser parser;
    private final DBCleaner cleaner;

    @Autowired
    public SystemStarter(ServiceConfigure serviceConfig, CCTSDocumentParser parser, DBCleaner cleaner) {
        this.serviceConfig = serviceConfig;
        this.parser = parser;
        this.cleaner = cleaner;
    }

    public void start(){
        // clean ccts db
        cleaner.cleanCCTSProfileDB();

        parser.parseAllCCTSProfilesAndSave2DB();

        isSystemReady = true;
    }

}
