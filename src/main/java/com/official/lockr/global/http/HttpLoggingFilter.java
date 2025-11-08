package com.official.lockr.global.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.auth.domain.signin.SignInSession;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private final HttpHeaders httpHeaders;
    private final HttpLoggingRepository httpLoggingRepository;
    private final ObjectMapper objectMapper;

    public HttpLoggingFilter(
            final HttpHeaders httpHeaders,
            final HttpLoggingRepository httpLoggingRepository,
            final ObjectMapper objectMapper
    ) {
        this.httpHeaders = httpHeaders;
        this.httpLoggingRepository = httpLoggingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        final HttpSession session = request.getSession(false);
        final HttpHeaderContext headerContext = new HttpHeaderContext(request);
        httpHeaders.set(headerContext);
        final var contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
        final var contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
        } finally {
            saveHttpRequest(session, headerContext, contentCachingRequestWrapper);
            saveHttpResponse(session, headerContext, contentCachingRequestWrapper, contentCachingResponseWrapper);
            contentCachingResponseWrapper.copyBodyToResponse();
        }
    }

    private void saveHttpRequest(
            final HttpSession httpSession,
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
                    userId(httpSession),
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

    private static String userId(final HttpSession httpSession) {
        if (Objects.isNull(httpSession)) {
            return "";
        }
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        return signIn.userId();
    }

    private void saveHttpResponse(
            final HttpSession httpSession,
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
                    userId(httpSession),
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
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        response::getHeader,
                        (existing, replacement) -> existing + ", " + replacement
                ));
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
