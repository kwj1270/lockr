package com.official.lockr.global.mvc;

import com.github.f4b6a3.ulid.UlidCreator;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HttpHeaderContext {

    private String rootGuid;
    private String childGuid;
    private String authorization;
    private String userAgent;
    private String acceptLanguage;
    private String xRequestId;
    private String xForwardedFor;
    private String deviceId;
    private String deviceInfo;
    private String ipAddress;
    private String appVersion;
    private Map<String, String> customHeaders = new HashMap<>();

    // Getters and Setters (중복 제거)
    public String getRootGuid() {
        return rootGuid;
    }

    public void setRootGuid(final String rootGuid) {
        this.rootGuid = Strings.isNotBlank(rootGuid) ? rootGuid : UlidCreator.getUlid().toString();
    }

    public String getChildGuid() {
        return childGuid;
    }

    public void setChildGuid(final String childGuid) {
        this.childGuid = childGuid(childGuid);
    }

    private String childGuid(final String childGuid) {
        if (Strings.isBlank(this.rootGuid)) {
            setRootGuid("");
        }
        if (Strings.isBlank(childGuid)) {
            return this.rootGuid + "0001";
        }
        final String numericPart = childGuid.substring(childGuid.length() - 4);
        final int nextNumber = Integer.parseInt(numericPart) + 1;
        return this.rootGuid + String.format("%04d", nextNumber);
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    // 중복 제거: XRequestId만 남김
    public String getXRequestId() {
        return xRequestId;
    }

    public void setXRequestId(String xRequestId) {
        this.xRequestId = xRequestId;
    }

    // 중복 제거: XForwardedFor만 남김
    public String getXForwardedFor() {
        return xForwardedFor;
    }

    public void setXForwardedFor(String xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceInfo(final String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setAppVersion(final String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    // 편의 메서드들
    public void addCustomHeader(String key, String value) {
        this.customHeaders.put(key, value);
    }

    public String getCustomHeader(String key) {
        return this.customHeaders.get(key);
    }

    public boolean hasAuthorization() {
        return authorization != null && !authorization.isEmpty();
    }

    // GUID 관련 편의 메서드 추가
    public boolean hasRootGuid() {
        return rootGuid != null && !rootGuid.isEmpty();
    }

    public boolean hasChildGuid() {
        return childGuid != null && !childGuid.isEmpty();
    }

    @Override
    public String toString() {
        return "RequestHeaderContext{" +
                "rootGuid='" + rootGuid + '\'' +
                ", childGuid='" + childGuid + '\'' +
                ", authorization='" + maskSensitiveData(authorization) + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", acceptLanguage='" + acceptLanguage + '\'' +
                ", xRequestId='" + xRequestId + '\'' +
                ", xForwardedFor='" + xForwardedFor + '\'' +
                ", customHeaders=" + customHeaders +
                '}';
    }

    private String maskSensitiveData(String data) {
        return data != null && data.length() > 10 ? data.substring(0, 10) + "***" : data;
    }
}
