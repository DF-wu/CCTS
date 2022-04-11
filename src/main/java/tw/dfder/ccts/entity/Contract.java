package tw.dfder.ccts.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;

@Document(collection = "Contracts")
public class Contract {
    @Id
    private String Id;

    @Field
    private String consumerName;

    @Field
    private String providerName;

    @Field
    private ArrayList<String> contractTerms;


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public ArrayList<String> getContractTerms() {
        return contractTerms;
    }

    public void setContractTerms(ArrayList<String> contractTerms) {
        this.contractTerms = contractTerms;
    }
}
