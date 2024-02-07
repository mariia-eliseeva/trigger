package config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"core", "persistence", "gpt", "config", "userresponse"})
public class TlgbotserviceApplication {


    public static void main(String[] args) {
        SpringApplication.run(TlgbotserviceApplication.class, args);
    }
}

