package com.official.lockr.global.http;

import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpHeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    private final HttpHeaderContextThreadLocal httpHeaderContextThreadLocal;

    public HttpHeaderPropagationInterceptor(final HttpHeaderContextThreadLocal httpHeaderContextThreadLocal) {
        this.httpHeaderContextThreadLocal = httpHeaderContextThreadLocal;
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
        final HttpHeaderContext headerContext = httpHeaderContextThreadLocal.get();
        try {
            addHeaderIfNotBlank(request, "X-ROOT-GUID", headerContext.rootGuid());
            addHeaderIfNotBlank(request, "X-CHILD-GUID", headerContext.childGuid());
            addHeaderIfNotBlank(request, "Authorization", headerContext.authorization());
            addHeaderIfNotBlank(request, "User-Agent", headerContext.userAgent());
            addHeaderIfNotBlank(request, "Accept-Language", headerContext.acceptLanguage());
            addHeaderIfNotBlank(request, "X-Request-ID", headerContext.xRequestId());
            addHeaderIfNotBlank(request, "X-Forwarded-For", headerContext.xForwardedFor());
            addHeaderIfNotBlank(request, "X-DEVICE-ID", headerContext.deviceId());
            addHeaderIfNotBlank(request, "X-DEVICE-INFO", headerContext.deviceInfo());
            addHeaderIfNotBlank(request, "X-IP-ADDRESS", headerContext.ipAddress());
            addHeaderIfNotBlank(request, "X-APP-VERSION", headerContext.ipAddress());
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
