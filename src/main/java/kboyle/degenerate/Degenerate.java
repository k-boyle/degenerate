package kboyle.degenerate;

import kboyle.degenerate.configuration.DegenerateConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableConfigurationProperties(DegenerateConfig.class)
@EnableTransactionManagement
@EnableJpaRepositories
public class Degenerate {
    public static void main(String[] args) {
        SpringApplication.run(Degenerate.class, args);
    }
}
