package com.official.lockr.config.global.http;

public interface HttpLoggingRepository {
    void save(String rootGuid,
              String childGuid,
              String txDate,
              String txTime,
              String clientIp,
              String userId,
              String httpMethod,
              String path,
              String statusCode,
              String headers,
              String body
    );
}
