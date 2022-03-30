package tw.dfder.ccts.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Contracts")
public class ServiceContract {
    @Id
    private String Id;

    @Field
    private String consumer;
    @Field
    private String producer;




}
