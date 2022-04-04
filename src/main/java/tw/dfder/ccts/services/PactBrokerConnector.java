package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.UrlTemplate;

@Service
public class PactBrokerConnector {
    private final ServiceConfigure serviceConfig;

    @Autowired
    public PactBrokerConnector(ServiceConfigure serviceConfig) {
        this.serviceConfig = serviceConfig;
    }



    // retrieve from pact broker
    private ResponseEntity<?> sendGetRequest(String targetUrl) {
        if (targetUrl.equals("")) {
            System.out.println("!!! no target url provided");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Accept", MediaType.ALL_VALUE);
            httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(null, httpHeaders);
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.exchange(
                    targetUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        }catch (Exception e){
            System.out.println("!!!retrieve pact fail.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<?> retrieveAllPactsFromPactBroker(){
        return sendGetRequest(UrlTemplate.getAllPactsURLPath());
    }

    public ResponseEntity<?> retrievePactDetail(String providerName, String consumerName){
        String targetUrl = serviceConfig.pactBrokerUrlPrefix + UrlTemplate.getPactDetailURLPath(providerName, consumerName);
        return sendGetRequest(targetUrl);
    }




}
