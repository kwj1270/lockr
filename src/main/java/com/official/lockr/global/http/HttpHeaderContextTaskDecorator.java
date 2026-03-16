package com.official.lockr.global.http;

import org.springframework.core.task.TaskDecorator;

public class HttpHeaderContextTaskDecorator implements TaskDecorator {

    private final HttpHeaders httpHeaders;

    public HttpHeaderContextTaskDecorator(final HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @Override
    public Runnable decorate(final Runnable runnable) {
        final HttpHeaderContext context = httpHeaders.get();
        return () -> {
            try {
                if (context != null) {
                    httpHeaders.set(context);
                }
                runnable.run();
            } finally {
                httpHeaders.remove();
            }
        };
    }
}
