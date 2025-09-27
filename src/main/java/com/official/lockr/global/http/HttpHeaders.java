package com.official.lockr.global.http;

import org.springframework.stereotype.Component;

@Component
public class HttpHeaders {

    private final InheritableThreadLocal<HttpHeaderContext> inheritableThreadLocal = new InheritableThreadLocal<>();

    public HttpHeaderContext get() {
        return inheritableThreadLocal.get();
    }

    public void set(final HttpHeaderContext httpHeaderContext) {
        inheritableThreadLocal.set(httpHeaderContext);
    }
}
