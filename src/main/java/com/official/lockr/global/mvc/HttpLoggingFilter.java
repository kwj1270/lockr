package com.official.lockr.global.mvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.global.http.HttpLoggingRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    );

    private static final List<String> STANDARD_HEADERS = Arrays.asList(
            "authorization", "user-agent", "accept-language",
            "x-request-id", "x-forwarded-for", "x-root-guid", "x-child-guid"
    );

    private final HttpHeaderContext headerContext;
    private final HttpLoggingRepository httpLoggingRepository;
    private final ObjectMapper objectMapper;

    public HttpLoggingFilter(
            final HttpLoggingRepository httpLoggingRepository,
            final ObjectMapper objectMapper,
            final HttpHeaderContext headerContext
    ) {
        this.headerContext = headerContext;
        this.httpLoggingRepository = httpLoggingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        populateRequestHeaderContext(request);
        MDC.put("traceId", headerContext.getRootGuid());

        final var contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
        final var contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        saveHttpRequest(headerContext.getRootGuid(), headerContext.getChildGuid(), contentCachingRequestWrapper);

        try {
            filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
        } finally {
            contentCachingResponseWrapper.copyBodyToResponse();
            saveHttpResponse(headerContext.getRootGuid(), headerContext.getChildGuid(), contentCachingRequestWrapper, contentCachingResponseWrapper);
        }
    }

    private void saveHttpRequest(
            final String rootGuid,
            final String childGuid,
            final ContentCachingRequestWrapper request
    ) {
        try {
            httpLoggingRepository.save(
                    rootGuid,
                    childGuid,
                    LocalDate.now().toString(),
                    LocalTime.now().toString(),
                    getClientIpAddress(request),
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
            final String rootGuid,
            final String childGuid,
            final ContentCachingRequestWrapper request,
            final ContentCachingResponseWrapper response
    ) {
        try {
            httpLoggingRepository.save(
                    rootGuid,
                    childGuid,
                    LocalDate.now().toString(),
                    LocalTime.now().toString(),
                    getClientIpAddress(request),
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

    private String getClientIpAddress(final HttpServletRequest request) {
        return IP_HEADERS.stream()
                .map(request::getHeader)
                .filter(ipAddress -> ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress))
                .findFirst()
                .orElse(request.getRemoteAddr());
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

    private void populateRequestHeaderContext(final HttpServletRequest request) {
        try {
            headerContext.setRootGuid(request.getHeader("X-ROOT-GUID"));
            headerContext.setChildGuid(request.getHeader("X-CHILD-GUID"));
            headerContext.setAppVersion(request.getHeader("X-APP-VERSION"));
            headerContext.setIpAddress(getClientIpAddress(request));
            headerContext.setDeviceId(request.getHeader("X-DEVICE-ID"));
            headerContext.setDeviceInfo(request.getHeader("X-DEVICE-INFO"));
            headerContext.setAuthorization(request.getHeader("Authorization"));
            headerContext.setUserAgent(request.getHeader("User-Agent"));
            headerContext.setAcceptLanguage(request.getHeader("Accept-Language"));
            headerContext.setXRequestId(request.getHeader("X-Request-ID"));
            headerContext.setXForwardedFor(getClientIpAddress(request));

            // 기타 커스텀 헤더들도 저장
            Collections.list(request.getHeaderNames())
                    .forEach(headerName -> {
                        if (!STANDARD_HEADERS.contains(headerName.toLowerCase())) {
                            headerContext.addCustomHeader(headerName, request.getHeader(headerName));
                        }
                    });
        } catch (Exception e) {
            logger.warn("Failed to populate RequestHeaderContext", e);
        }
    }
}
