package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tw.dfder.ccts.configuration.ServiceConfigure;

@Service
public class ServiceConnector {
    public static final String URLPOSTFIX_allPactsLatest = "/pacts/latest";
    private final ServiceConfigure serviceConfig;


    @Autowired
    public ServiceConnector(ServiceConfigure serviceConfig) {
        this.serviceConfig = serviceConfig;
    }



    // retrive from pact broker
    private ResponseEntity<String> sendGetRequest(String postfix) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.ALL_VALUE);
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(null, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(
                serviceConfig.pactBrokerUrlPrefix + postfix,
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    public ResponseEntity<String> retrieveAllPactsFromPactBroker(){
        return sendGetRequest(URLPOSTFIX_allPactsLatest);
    }


}
