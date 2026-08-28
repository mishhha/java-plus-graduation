package evm.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import evm.users.UserServiceApplication;
import evm.event.EventServiceApplication;

@SpringBootApplication
@ComponentScan(
    basePackages = "evm",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            UserServiceApplication.class,
            EventServiceApplication.class
            // MainServiceApplication убран, так как main-service нет в зависимостях pom.xml
        }
    )
)
@EntityScan(basePackages = "evm")
@EnableJpaRepositories(basePackages = "evm")
public class RequestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}