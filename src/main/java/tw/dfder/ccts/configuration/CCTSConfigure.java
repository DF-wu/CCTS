package tw.dfder.ccts.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class CCTSConfigure {
    @Value("classpath:static/cctsProfile.yaml")
    public Resource cctsFile;


}
