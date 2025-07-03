package io.watch.search.config.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchJavaApiClientConfig {

    @Value("${spring.elasticsearch.uris}")
    private String uris;

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient() {
        String firstUri = uris.split(",")[0].replace("http://", "").replace("https://", "");
        String host = firstUri.split(":")[0];
        int port = Integer.parseInt(firstUri.split(":")[1]);

        RestClient restClient = RestClient.builder(new HttpHost(host, port)).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchAsyncClient(transport);
    }
}
