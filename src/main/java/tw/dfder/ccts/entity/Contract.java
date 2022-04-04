package tw.dfder.ccts.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Contracts")
public class Contract {
    @Id
    private String Id;

    @Field
    private String consumerName;

    @Field
    private String providerName;






}
