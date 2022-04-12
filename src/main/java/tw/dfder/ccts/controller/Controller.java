package tw.dfder.ccts.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tw.dfder.ccts.configuration.ServiceConfigure;
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
    @Autowired
    public Controller(CCTSDocumentParser CCTSDocumentParser, PactBrokerBusyBox pactBrokerBusyBox, ServiceConfigure serviceConfig, SystemStarter starter) {
        this.CCTSDocumentParser = CCTSDocumentParser;
        this.pactBrokerBusyBox = pactBrokerBusyBox;
        this.serviceConfig = serviceConfig;
        this.starter = starter;
    }


    @PostMapping("/start")
    public ResponseEntity<?> startCCTSVerification(){
        starter.startCCTSTest();
        return new ResponseEntity<>(HttpStatus.OK);
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



        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        pactBrokerBusyBox.retrieveAllPacts();

        Process process;


//
//        process = Runtime.getRuntime().exec("docker run --rm  pactfoundation/pact-cli:latest broker can-i-deploy --pacticipant orchestrator --latest --broker-base-url http://23.dfder.tw:10141");
//        StreamGobbler streamGobbler =
//                new StreamGobbler(process.getInputStream(), System.out::println);
//        Executors.newSingleThreadExecutor().submit(streamGobbler);
//        int exitCode = process.waitFor();
//        System.out.println("!!!!!");
//        System.out.println(exitCode);


        pactBrokerBusyBox.getContractFromBroker("loggingService", "orchestrator");

    }






}
