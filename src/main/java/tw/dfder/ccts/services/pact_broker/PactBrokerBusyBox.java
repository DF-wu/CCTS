package tw.dfder.ccts.services.pact_broker;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.Contract;

import java.util.ArrayList;

@Service("PactBrokerBusyBox")
public class PactBrokerBusyBox {
    private Gson gson;
    private ServiceConfigure serviceConfig;
    private PactBrokerConnector pactBrokerConnector;



    @Autowired
    public PactBrokerBusyBox(Gson gson, ServiceConfigure serviceConfig, PactBrokerConnector pactBrokerConnector) {
        this.gson = gson;
        this.serviceConfig = serviceConfig;
        this.pactBrokerConnector = pactBrokerConnector;
    }

    public ArrayList<String> retrieveAllPacts(){
        // I am JsonObject from Gson!!
        JsonObject pacts =  gson.fromJson(pactBrokerConnector.retrieveAllPactsFromPactBroker().getBody().toString(), JsonObject.class);

        // consumer , provider
        ArrayList<String> pairs = new ArrayList<>();
        pacts.getAsJsonArray("pacts").forEach(obj ->{

                obj.getAsJsonObject()
                        .get("_embedded")
                        .getAsJsonObject()
                        .get("consumer")
                        .getAsJsonObject()
                        .get("name");

            System.out.println(
                    obj.getAsJsonObject()
                            .get("_embedded")
                            .getAsJsonObject()
                            .get("provider")
                            .getAsJsonObject()
                            .get("name"));
                });

        return null;
    }



    public Contract getContractFromBroker(String providerName, String consumerName){
        JsonObject pactJson = gson.fromJson(
                pactBrokerConnector.retrieveLatestPactDetail(providerName, consumerName).getBody().toString(),
                JsonObject.class
        );

        Contract c = new Contract();
        c.setConsumerName(pactJson.getAsJsonObject().get("consumer").getAsJsonObject().get("name").getAsString());
        c.setProviderName(pactJson.getAsJsonObject().get("provider").getAsJsonObject().get("name").getAsString());


        // get all contract testcaseid in pact
        ArrayList<String> terms = new ArrayList<>();

        for (JsonElement e:
        pactJson.getAsJsonObject().get("messages").getAsJsonArray()) {
            terms.add(e.getAsJsonObject().get("description").getAsString());
        }
        c.setTestCaseIds(terms);
        return c;

    }


}
