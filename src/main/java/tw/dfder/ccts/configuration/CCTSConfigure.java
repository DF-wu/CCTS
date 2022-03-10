package tw.dfder.ccts.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class CCTSConfigure {
    // not working
//    @Value("classpath:static/cctsProfile.yaml")
//    public static Resource cctsFile;

    public static Resource cctsFile = new ClassPathResource("static/cctsProfile.yaml");


}
