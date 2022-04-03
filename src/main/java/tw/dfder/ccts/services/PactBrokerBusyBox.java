package tw.dfder.ccts.services;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.configuration.ServiceConfigure;

import java.util.ArrayList;

@Service("PactBrokerBusyBox")
public class PactBrokerBusyBox {
    private Gson gson;
    private ServiceConfigure serviceConfig;
    private ServiceConnector serviceConnector;



    @Autowired
    public PactBrokerBusyBox(Gson gson, ServiceConfigure serviceConfig, ServiceConnector serviceConnector) {
        this.gson = gson;
        this.serviceConfig = serviceConfig;
        this.serviceConnector = serviceConnector;
    }

    public ArrayList<String> getretrieveAllPacts(){
        // I am JsonObject from Gson!!
        JsonObject pacts =  gson.fromJson(serviceConnector.retrieveAllPactsFromPactBroker().getBody(), JsonObject.class);

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



}
