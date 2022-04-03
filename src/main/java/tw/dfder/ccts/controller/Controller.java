package tw.dfder.ccts.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import tw.dfder.ccts.entity.CCTSProfile;
import tw.dfder.ccts.services.CCTSProfileParser;
import tw.dfder.ccts.services.PactBrokerBusyBox;
import tw.dfder.ccts.services.StreamGobbler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.Executors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class Controller {

    CCTSProfileParser CCTSProfileParser;
    private PactBrokerBusyBox pactBrokerBusyBox;

    @Autowired
    public Controller(CCTSProfileParser CCTSProfileParser, PactBrokerBusyBox pactBrokerBusyBox) {
        this.CCTSProfileParser = CCTSProfileParser;
        this.pactBrokerBusyBox = pactBrokerBusyBox;
    }

    @PostConstruct
    public void testMethod() throws IOException, InterruptedException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        CCTSProfile cctsProfile = CCTSProfileParser.parse2CCTSProfile();

        System.out.println(CCTSProfileParser.findPathList(cctsProfile).size());

//        pactBrokerBusyBox.retrieveAllPacts();

        Process process;



        process = Runtime.getRuntime().exec("docker run --rm -e 23.dfder.tw:10141 pactfoundation/pact-cli:latest broker can-i-deploy --pacticipant loggingService --latest");
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        System.out.println("!!!!!");
        System.out.println(exitCode);


    }

}
