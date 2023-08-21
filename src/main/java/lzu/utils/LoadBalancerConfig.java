package lzu.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalancerConfig {

    @Bean
    public LoadBalancer loadBalancer() {
        return new LoadBalancer(3); // for example, 3 instances
    }
}

