package com.official.lockr.global.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private final HttpHeaderContextThreadLocal httpHeaderContextThreadLocal;
    private final HttpLoggingRepository httpLoggingRepository;
    private final ObjectMapper objectMapper;

    public HttpLoggingFilter(
            final HttpHeaderContextThreadLocal httpHeaderContextThreadLocal,
            final HttpLoggingRepository httpLoggingRepository,
            final ObjectMapper objectMapper
    ) {
        this.httpHeaderContextThreadLocal = httpHeaderContextThreadLocal;
        this.httpLoggingRepository = httpLoggingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {

        final HttpHeaderContext headerContext = new HttpHeaderContext(request);
        httpHeaderContextThreadLocal.set(headerContext);

        final var contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
        final var contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        saveHttpRequest(headerContext, contentCachingRequestWrapper);

        try {
            filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
        } finally {
            contentCachingResponseWrapper.copyBodyToResponse();
            saveHttpResponse(headerContext, contentCachingRequestWrapper, contentCachingResponseWrapper);
        }
    }

    private void saveHttpRequest(
            final HttpHeaderContext headerContext,
            final ContentCachingRequestWrapper request
    ) {
        try {
            httpLoggingRepository.save(
                    headerContext.rootGuid(),
                    headerContext.childGuid(),
                    LocalDate.now().toString(),
                    LocalTime.now().toString(),
                    headerContext.ipAddress(),
                    "test",
                    request.getMethod(),
                    request.getRequestURI(),
                    "0000",
                    convertStringToJson(getHeadersAsString(request)),
                    convertStringToJson(getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding()))
            );
        } catch (Exception e) {
            logger.error("Failed to save HTTP information", e);
        }
    }

    private void saveHttpResponse(
            final HttpHeaderContext headerContext,
            final ContentCachingRequestWrapper request,
            final ContentCachingResponseWrapper response
    ) {
        try {
            httpLoggingRepository.save(
                    headerContext.rootGuid(),
                    headerContext.childGuid(),
                    LocalDate.now().toString(),
                    LocalTime.now().toString(),
                    headerContext.ipAddress(),
                    "test",
                    request.getMethod(),
                    request.getRequestURI(),
                    String.valueOf(response.getStatus()),
                    convertStringToJson(getHeadersAsString(response)),
                    convertStringToJson(getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding()))
            );
        } catch (Exception e) {
            logger.error("Failed to save HTTP information", e);
        }
    }

    private String getHeadersAsString(final HttpServletRequest request) throws JsonProcessingException {
        final Map<String, String> headers = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(headerName -> headerName, request::getHeader));
        return objectMapper.writeValueAsString(headers);
    }

    private String getHeadersAsString(final HttpServletResponse response) throws JsonProcessingException {
        final Map<String, String> headers = response.getHeaderNames().stream()
                .collect(Collectors.toMap(headerName -> headerName, response::getHeader));
        return objectMapper.writeValueAsString(headers);
    }

    private String getContentAsString(final byte[] content, final String characterEncoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        try {
            return new String(content, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to get content as string", e);
            return "Unsupported Encoding";
        }
    }

    public String convertStringToJson(String inputString) {
        if (Strings.isBlank(inputString)) {
            return "{}";
        }
        try {
            objectMapper.readTree(inputString);
            return inputString;
        } catch (JsonProcessingException e) {
            final Map<String, String> defaultMap = new HashMap<>();
            defaultMap.put("default", inputString);
            try {
                return objectMapper.writeValueAsString(defaultMap);
            } catch (JsonProcessingException ex) {
                return "{}";
            }
        }
    }
}
