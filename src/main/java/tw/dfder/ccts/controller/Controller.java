package tw.dfder.ccts.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.services.CCTSDocumentParser;
import tw.dfder.ccts.services.DBCleaner;
import tw.dfder.ccts.services.SystemStarter;
import tw.dfder.ccts.services.pact_broker.PactBrokerBusyBox;

import javax.annotation.PostConstruct;
import java.io.IOException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class Controller {

    private CCTSDocumentParser CCTSDocumentParser;
    private PactBrokerBusyBox pactBrokerBusyBox;
    private ServiceConfigure serviceConfig;
    private DBCleaner dbcleaner;
    private final SystemStarter starter;


    // for developing only
    private final CCTSDocumentRepository repo;
    @Autowired
    public Controller(CCTSDocumentParser CCTSDocumentParser, PactBrokerBusyBox pactBrokerBusyBox, ServiceConfigure serviceConfig, DBCleaner dbcleaner, SystemStarter starter, CCTSDocumentRepository repo) {
        this.CCTSDocumentParser = CCTSDocumentParser;
        this.pactBrokerBusyBox = pactBrokerBusyBox;
        this.serviceConfig = serviceConfig;
        this.dbcleaner = dbcleaner;
        this.starter = starter;
        this.repo = repo;
    }


    @PostMapping("/start")
    public ResponseEntity<?> startCCTSVerification(){
        CCTSResult result = starter.startCCTSTest();
        if (result != null){
            return new ResponseEntity<>( result.checkOutReportMessage() ,HttpStatus.OK);
        }else {
            return new ResponseEntity<>("System not ready", HttpStatus.OK);
        }
    }

    @PostMapping("/cleanDB")
    public ResponseEntity<?> cleanAllDB(){
        dbcleaner.cleanEventLogDB();
        dbcleaner.cleanCCTSProfileDB();
        return new ResponseEntity<>(HttpStatus.OK);
    }



    @PostConstruct
    public void systemStarterEntryPoint(){
        //  load all ccts profile
        starter.systemInit();

    }

    @PostConstruct
    public void testMethod() throws IOException, InterruptedException {
        for(CCTSDocument d: repo.findAll()){
            CCTSDocumentParser.findPathList(d) ;
        }



        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        pactBrokerBusyBox.retrieveAllPacts();

        Process process;

//        pactBrokerBusyBox.getContractFromBroker("loggingService", "orchestrator");

    }






}
