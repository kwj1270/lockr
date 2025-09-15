package com.official.lockr.global.http;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.List;

public record HttpHeaderContext(
        String rootGuid, String childGuid, String authorization, String userAgent,
        String acceptLanguage, String xRequestId, String xForwardedFor, String deviceId,
        String deviceInfo, String ipAddress, String appVersion
) {

    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    );

    public HttpHeaderContext(final HttpServletRequest request) {
        this(
                request.getHeader("X-ROOT-GUID"),
                request.getHeader("X-CHILD-GUID"),
                request.getHeader("Authorization"),
                request.getHeader("User-Agent"),
                request.getHeader("Accept-Language"),
                request.getHeader("X-Request-ID"),
                request.getHeader("X-FORWARDED-FOR"),
                request.getHeader("X-DEVICE-ID"),
                request.getHeader("X-DEVICE-INFO"),
                clientIpAddress(request),
                request.getHeader("X-APP-VERSION")
        );
    }


    public HttpHeaderContext(final String rootGuid, final String childGuid, final String authorization,
                             final String userAgent, final String acceptLanguage, final String xRequestId,
                             final String xForwardedFor, final String deviceId, final String deviceInfo,
                             final String ipAddress, final String appVersion) {
        this.rootGuid = createRootGuid(rootGuid);
        this.childGuid = createChildGuid(this.rootGuid, childGuid);
        this.authorization = authorization;
        this.userAgent = userAgent;
        this.acceptLanguage = acceptLanguage;
        this.xRequestId = xRequestId;
        this.xForwardedFor = xForwardedFor;
        this.deviceId = deviceId;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.appVersion = appVersion;
    }

    private static String createRootGuid(final String rootGuid) {
        return Strings.isNotBlank(rootGuid) ? rootGuid : UlidCreator.getUlid().toString();
    }

    private static String createChildGuid(final String rootGuid, final String childGuid) {
        if (Strings.isBlank(childGuid)) {
            return rootGuid + "0001";
        }
        final String numericPart = childGuid.substring(childGuid.length() - 4);
        final int nextNumber = Integer.parseInt(numericPart) + 1;
        return rootGuid + String.format("%04d", nextNumber);
    }

    private static String clientIpAddress(final HttpServletRequest request) {
        if (Strings.isNotBlank(request.getHeader("X-IP-ADDRESS"))) {
            return request.getHeader("X-IP-ADDRESS");
        }
        return IP_HEADERS.stream()
                .map(request::getHeader)
                .filter(ipAddress -> ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress))
                .findFirst()
                .orElse(request.getRemoteAddr());
    }

    @Override
    public String toString() {
        return "HttpHeaderContext{" +
                "rootGuid='" + rootGuid + '\'' +
                ", childGuid='" + childGuid + '\'' +
                ", authorization='" + maskSensitiveData(authorization) + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", acceptLanguage='" + acceptLanguage + '\'' +
                ", xRequestId='" + xRequestId + '\'' +
                ", xForwardedFor='" + xForwardedFor + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }

    private String maskSensitiveData(String data) {
        return data != null && data.length() > 10 ? data.substring(0, 10) + "***" : data;
    }
}
