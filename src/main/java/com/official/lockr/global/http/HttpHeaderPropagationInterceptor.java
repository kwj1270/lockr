package com.official.lockr.global.http;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpHeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpHeaderPropagationInterceptor.class);

    private final HttpHeaders httpHeaders;

    public HttpHeaderPropagationInterceptor(final HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
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
        final HttpHeaderContext headerContext = httpHeaders.get();
        if (headerContext == null) {
            return;
        }
        try {
            addHeaderIfNotBlank(request, "X-ROOT-GUID", headerContext.rootGuid());
            addHeaderIfNotBlank(request, "X-CHILD-GUID", headerContext.childGuid());
            addHeaderIfNotBlank(request, "Authorization", headerContext.authorization());
            addHeaderIfNotBlank(request, "User-Agent", headerContext.userAgent());
            addHeaderIfNotBlank(request, "Accept-Language", headerContext.acceptLanguage());
            addHeaderIfNotBlank(request, "X-Request-ID", headerContext.xRequestId());
            addHeaderIfNotBlank(request, "X-Forwarded-For", headerContext.xForwardedFor());
            addHeaderIfNotBlank(request, "X-DEVICE-ID", headerContext.deviceId());
            addHeaderIfNotBlank(request, "X-DEVICE-NAME", headerContext.deviceName());
            addHeaderIfNotBlank(request, "X-DEVICE-OS", headerContext.deviceOS());
            addHeaderIfNotBlank(request, "X-IP-ADDRESS", headerContext.ipAddress());
            addHeaderIfNotBlank(request, "X-APP-VERSION", headerContext.appVersion());
        } catch (Exception e) {
            log.error("Failed to propagate headers: {}", e.getMessage(), e);
        } finally {
            httpHeaders.set(headerContext.increaseChildGuid());
        }
    }

    private void addHeaderIfNotBlank(final HttpRequest request, final String headerName, final String headerValue) {
        if (Strings.isNotBlank(headerValue)) {
            request.getHeaders().add(headerName, headerValue);
        }
    }
}
