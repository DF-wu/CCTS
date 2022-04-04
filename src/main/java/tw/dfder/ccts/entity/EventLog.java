package tw.dfder.ccts.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "EventLogs")
public class EventLog {
    @Id
    private String Id;

    // what CCTS test case this message belongs to.
    @Field
    private String CCTSProfileName;

    // when it parsed to this obj
    @Field
    private long timeInMillis;

    @Field
    private String providerName;
    @Field
    private String consumerName;
    @Field
    private String contractName;
    @Field
    private String contractDescription;

}
