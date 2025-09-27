package com.official.lockr.global.http;

import com.github.f4b6a3.ulid.UlidCreator;
import org.apache.logging.log4j.util.Strings;
import org.jooq.Configuration;
import org.jooq.JSON;
import org.jooq.generated.tables.daos.HttpLogDao;
import org.jooq.generated.tables.pojos.HttpLogEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JOOQHttpLoggingRepository implements HttpLoggingRepository {

    private final HttpLogDao httpLogDao;

    public JOOQHttpLoggingRepository(final Configuration configuration) {
        this.httpLogDao = new HttpLogDao(configuration);
    }

    @Transactional
    @Override
    public void save(final String rootGuid,
                     final String childGuid,
                     final String txDate,
                     final String txTime,
                     final String clientIp,
                     final String userId,
                     final String httpMethod,
                     final String path,
                     final String statusCode,
                     final String headers,
                     final String body
    ) {
        final HttpLogEntity httpLogs = new HttpLogEntity(
                UlidCreator.getUlid().toString(),
                rootGuid,
                childGuid,
                txDate,
                txTime,
                clientIp,
                userId,
                httpMethod,
                path,
                Short.parseShort(statusCode),
                Strings.isBlank(headers) ? JSON.valueOf("{}") : JSON.valueOf(headers),
                Strings.isBlank(body) ? JSON.valueOf("{}") : JSON.valueOf(body)
        );
        httpLogDao.insert(httpLogs);
    }
}
