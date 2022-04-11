package tw.dfder.ccts.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ServiceConfigure {
    @Value("classpath:CCTSDocuments/*.yaml")
    public Resource[] cctsFiles;

    @Value("${CCTS.pact_broker}")
    public String pactBrokerUrlPrefix;

}
