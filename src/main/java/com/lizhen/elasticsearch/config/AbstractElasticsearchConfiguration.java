package com.lizhen.elasticsearch.config;

import com.lizhen.elasticsearch.ElasticsearchOperations;
import com.lizhen.elasticsearch.ElasticsearchRestTemplate;
import com.lizhen.elasticsearch.convert.ElasticsearchConverter;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;

/**
 * @author Christoph Strobl
 * @author Peter-Josef Meisch
 * @since 3.2
 * @see ElasticsearchConfigurationSupport
 */
public abstract class AbstractElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

    /**
     * Return the {@link RestHighLevelClient} instance used to connect to the cluster. <br />
     *
     * @return never {@literal null}.
     */
    @Bean
    public abstract RestHighLevelClient elasticsearchClient();

    /**
     * Creates {@link ElasticsearchOperations}.
     *
     * @return never {@literal null}.
     */
    @Bean(name = { "elasticsearchOperations", "elasticsearchTemplate" })
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchConverter elasticsearchConverter,
                                                           RestHighLevelClient elasticsearchClient) {

        ElasticsearchRestTemplate template = new ElasticsearchRestTemplate(elasticsearchClient, elasticsearchConverter);
        template.setRefreshPolicy(refreshPolicy());

        return template;
    }
}