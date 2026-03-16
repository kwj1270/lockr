package com.official.lockr.global.http;

import org.springframework.stereotype.Component;

@Component
public class HttpHeaders {

    private final ThreadLocal<HttpHeaderContext> threadLocal = new ThreadLocal<>();

    public HttpHeaderContext get() {
        return threadLocal.get();
    }

    public void set(final HttpHeaderContext httpHeaderContext) {
        threadLocal.set(httpHeaderContext);
    }

    public void remove() {
        threadLocal.remove();
    }
}
