package com.official.lockr.config.global.mvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.official.lockr.config.global.http.HttpLoggingRepository;
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

    private static final List<String> IP_HEADER_CANDIDATES = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );

    private final HttpLoggingRepository httpLoggingRepository;
    private final ObjectMapper objectMapper;

    public HttpLoggingFilter(final HttpLoggingRepository httpLoggingRepository, final ObjectMapper objectMapper) {
        this.httpLoggingRepository = httpLoggingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain
    ) throws ServletException, IOException {
        final String rootGuid = getRootGuid(request);
        final String childGuid = getChildGuid(rootGuid, request);
        MDC.put("traceId", rootGuid);

        final var contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
        final var contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        saveHttpRequest(rootGuid, childGuid, contentCachingRequestWrapper);

        try {
            filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
        } finally {
            saveHttpResponse(rootGuid, childGuid, contentCachingRequestWrapper, contentCachingResponseWrapper);
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
        return IP_HEADER_CANDIDATES.stream()
                .map(request::getHeader)
                .filter(ipAddress -> ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress))
                .findFirst()
                .orElse(request.getRemoteAddr());
    }

    private String getRootGuid(final HttpServletRequest request) {
        final String rootGuid = request.getHeader("X-ROOT-GUID");
        return Strings.isNotBlank(rootGuid) ? rootGuid : UlidCreator.getUlid().toString();
    }

    private String getChildGuid(final String rootGuid, final HttpServletRequest request) {
        final String childGuid = request.getHeader("X-CHILD-GUID");
        if (Strings.isBlank(childGuid)) {
            return rootGuid + "0001";
        }
        final String numericPart = childGuid.substring(childGuid.length() - 4);
        final int nextNumber = Integer.parseInt(numericPart) + 1;
        return rootGuid + String.format("%04d", nextNumber);
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
