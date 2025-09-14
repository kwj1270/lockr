package com.official.lockr.domain.auth.infrastructure.http;

import com.official.lockr.global.mvc.HttpHeaderPropagationInterceptor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Configuration
public class HttpSignUpAuthorizationRestClientFactory {

    private final HttpHeaderPropagationInterceptor httpHeaderPropagationInterceptor;

    public HttpSignUpAuthorizationRestClientFactory(final HttpHeaderPropagationInterceptor httpHeaderPropagationInterceptor) {
        this.httpHeaderPropagationInterceptor = httpHeaderPropagationInterceptor;
    }

    @Bean(name = "signUpAuthorizationRestClient")
    public RestClient signUpAuthorizationRestClient(
            final RestClient.Builder restClientBuilder,
            @Value("${server.port}") final String port
    ) {
        return restClientBuilder
                .baseUrl(URI.create("http://localhost:" + port))
                .requestFactory(clientHttpRequestFactory())
                .requestInterceptor(httpHeaderPropagationInterceptor)
                .build();
    }

    private static ClientHttpRequestFactory clientHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    private static HttpClient httpClient() {
        return HttpClients.custom()
                .setConnectionManager(getConnectionManager())
                .setDefaultRequestConfig(requestConfig())
                .build();
    }

    private static RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(3))            // 연결 타임아웃
                .setResponseTimeout(Timeout.ofSeconds(3))           // 응답 타임아웃
                .setConnectionRequestTimeout(Timeout.ofSeconds(3))  // 커넥션 풀 타임아웃
                .build();
    }

    private static PoolingHttpClientConnectionManager getConnectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(10)                               // 전체 최대 커넥션
                .setConnectionTimeToLive(TimeValue.ofSeconds(3))   // 커넥션 TTL
                .build();
    }
}
