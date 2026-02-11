package com.official.lockr.global.util;

import java.util.regex.Pattern;

/**
 * 메시지 sanitize 유틸리티 - Stored XSS 방지
 * HTML 태그를 제거하되 마크다운 문법은 보존한다.
 */
public class MessageSanitizer {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern DANGEROUS_PROTOCOL_PATTERN =
            Pattern.compile("(?i)(javascript|data|vbscript)\\s*:");

    private MessageSanitizer() {
    }

    /**
     * 메시지를 sanitize한다.
     * - HTML 태그 제거
     * - 위험 프로토콜(javascript:, data:, vbscript:) 차단
     * - 앞뒤 공백 trim
     *
     * @param message 원본 메시지
     * @return sanitize된 메시지
     * @throws IllegalArgumentException sanitize 후 빈 문자열이거나 최대 길이 초과 시
     */
    public static String sanitize(final String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        String sanitized = message;

        // HTML 태그 제거 (마크다운 **bold**, *italic*, ~~strike~~, `code` 등은 보존)
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");

        // 위험한 프로토콜 제거
        sanitized = DANGEROUS_PROTOCOL_PATTERN.matcher(sanitized).replaceAll("");

        sanitized = sanitized.trim();

        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty after sanitization");
        }

        if (sanitized.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "Message length exceeds maximum allowed length of " + MAX_MESSAGE_LENGTH);
        }

        return sanitized;
    }

    /**
     * 인용 콘텐츠를 sanitize한다.
     * null 또는 빈 문자열인 경우 null을 반환한다.
     *
     * @param content 원본 인용 콘텐츠
     * @return sanitize된 콘텐츠 또는 null
     */
    public static String sanitizeQuotedContent(final String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        String sanitized = content;
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = DANGEROUS_PROTOCOL_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = sanitized.trim();

        return sanitized.isEmpty() ? null : sanitized;
    }
}
