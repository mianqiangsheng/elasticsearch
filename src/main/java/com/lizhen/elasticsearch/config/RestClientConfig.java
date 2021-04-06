package com.lizhen.elasticsearch.config;

import com.lizhen.elasticsearch.client.ClientConfiguration;
import com.lizhen.elasticsearch.client.RestClients;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfig extends AbstractElasticsearchConfiguration {

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo("10.10.103.221:9200")
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}
