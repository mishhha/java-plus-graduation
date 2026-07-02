package evm.stat.client.config;

import evm.stat.client.StatsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsClientConfig {
    @Bean
    @ConditionalOnMissingBean   //для подмены бина моком в тестах
    public StatsClient statsClient(@Value("${stats-server.url:http://stats-server:9090}") String url) {
        return new StatsClient(url);
    }
}
