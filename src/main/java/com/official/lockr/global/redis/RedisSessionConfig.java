package com.official.lockr.global.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.config.SessionRepositoryCustomizer;

@Configuration
@EnableRedisHttpSession
public class RedisSessionConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.setDefaultSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public SessionRepositoryCustomizer<RedisIndexedSessionRepository> sessionRepositorySerializerCustomizer() {
        return (repo) -> repo.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
    }
}
