package pe.edu.pucp.orquestadorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrquestadorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrquestadorServiceApplication.class, args);
    }

    // Bean de RestTemplate — el lab pide "instanciar de forma manual un objeto RestTemplate"
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}