package tw.dfder.ccts.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import tw.dfder.ccts.entity.CCTSProfile;
import tw.dfder.ccts.services.CCTSProfileParser;
import tw.dfder.ccts.services.PactBrokerBusyBox;

import javax.annotation.PostConstruct;
import java.io.IOException;

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
    public void testMethod() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        CCTSProfile cctsProfile = CCTSProfileParser.parse2CCTSProfile();

        System.out.println(CCTSProfileParser.findPathList(cctsProfile).size());

        pactBrokerBusyBox.retrieveAllPacts();

    }

}
