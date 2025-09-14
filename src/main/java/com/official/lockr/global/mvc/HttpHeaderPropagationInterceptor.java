package com.official.lockr.global.mvc;

import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class HttpHeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    private final HttpHeaderContext headerContext;

    public HttpHeaderPropagationInterceptor(final HttpHeaderContext headerContext) {
        this.headerContext = headerContext;
    }

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request,
            final byte[] body,
            final ClientHttpRequestExecution execution
    ) throws IOException {
        propagateHeaders(request);
        return execution.execute(request, body);
    }

    private void propagateHeaders(final HttpRequest request) {
        try {
            addHeaderIfNotBlank(request, "X-ROOT-GUID", headerContext.getRootGuid());
            addHeaderIfNotBlank(request, "X-CHILD-GUID", headerContext.getChildGuid());
            addHeaderIfNotBlank(request, "Authorization", headerContext.getAuthorization());
            addHeaderIfNotBlank(request, "User-Agent", headerContext.getUserAgent());
            addHeaderIfNotBlank(request, "Accept-Language", headerContext.getAcceptLanguage());
            addHeaderIfNotBlank(request, "X-Request-ID", headerContext.getXRequestId());
            addHeaderIfNotBlank(request, "X-Forwarded-For", headerContext.getXForwardedFor());

            // 커스텀 헤더들
            Map<String, String> customHeaders = headerContext.getCustomHeaders();
            if (customHeaders != null && !customHeaders.isEmpty()) {
                customHeaders.forEach((key, value) -> {
                    if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
                        request.getHeaders().add(key, value);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to propagate headers: " + e.getMessage());
        }
    }

    private void addHeaderIfNotBlank(final HttpRequest request, final String headerName, final String headerValue) {
        if (Strings.isNotBlank(headerValue)) {
            request.getHeaders().add(headerName, headerValue);
        }
    }
}
