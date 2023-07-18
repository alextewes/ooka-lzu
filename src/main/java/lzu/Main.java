package lzu;

import cli.ComponentAssemblerCLI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        // ComponentAssemblerCLI.main(args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000") // Adjust the allowed origins according to your setup
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Allow specific methods
                        .allowedHeaders("*"); // Allow all headers
            }
        };
    }

}


