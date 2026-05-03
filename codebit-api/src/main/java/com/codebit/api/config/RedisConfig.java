package com.codebit.api.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置类
 * 职责：配置 Redis 连接、序列化方式、缓存管理器
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisTemplate（用于编程式操作 Redis）
     * @param connectionFactory Redis 连接工厂（Spring Boot 自动配置提供）
     * @return 配置好的 RedisTemplate 实例
     *
     * 作用：提供低级别的 Redis 操作 API，支持各种数据类型（String、Hash、List、Set 等）
     * 使用场景：需要手动操作 Redis 时注入此 Bean
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);


        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 创建 JSON 序列化器，使用自定义的 ObjectMapper（支持 Java 8 时间类型和多态）
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper());

        // 设置 value 的序列化器：使用 JSON 序列化（对象会被转为 JSON 字符串存储）
        template.setValueSerializer(jsonSerializer);

        // 设置 Hash 结构中 value 的序列化器：也使用 JSON 序列化
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 配置 RedisCacheManager（用于声明式缓存，配合 @Cacheable 注解使用）
     * @param connectionFactory Redis 连接工厂
     * @return 配置好的缓存管理器
     *
     * 作用：管理 Spring 缓存抽象，处理 @Cacheable、@CacheEvict 等注解
     * 使用场景：通过注解方式缓存方法返回值
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建默认缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存过期时间：5分钟（过期后自动从 Redis 删除）
                .entryTtl(Duration.ofMinutes(5))

                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                // 设置 value 的序列化器：JSON 序列化（注意：这里没有使用自定义 ObjectMapper，可能有问题，所以加上）
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 配置 ObjectMapper（Jackson 核心配置）
     * 用于定制 JSON 序列化/反序列化行为
     *
     * @return 配置好的 ObjectMapper 实例
     *
     * 注意：该方法被 redisTemplate 调用，确保 Redis 中的 JSON 格式统一
     */
    private ObjectMapper objectMapper() {
        // 创建 ObjectMapper 实例（Jackson 的核心类，负责 JSON 与 Java 对象的转换）
        ObjectMapper mapper = new ObjectMapper();

        // 注册 Java 8 时间模块
        // 作用：支持 LocalDate、LocalDateTime、Instant 等 Java 8 时间类型
        // 原因：默认 Jackson 无法序列化这些类型，会抛出异常
        mapper.registerModule(new JavaTimeModule());

        // 配置多态类型验证器（白名单模式）
        // 作用：防止反序列化漏洞（避免反序列化恶意类）
        // 原理：只有白名单中的类可以被反序列化，其他类被拒绝
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.codebit.api.") // 只允许你的项目包下的类
                .allowIfSubType("java.util.")                   // 允许常用集合类型（List、Map、Set等）
                .allowIfSubType("java.time.")                   // 允许时间类型（LocalDateTime、Instant等）
                .build();

        // 激活默认类型信息（用于多态序列化）
        // 参数2：DefaultTyping.NON_FINAL - 对非 final 类添加类型信息（包括 Object、接口、抽象类）
        // 参数3：As.PROPERTY - 将类型信息作为普通的 JSON 属性存储（默认字段名为 @class）
        // 作用：序列化时添加 @class 字段记录原始类型，反序列化时恢复具体类型
        mapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 精细化可见性设置

        // 设置字段可见性：ANY - 所有字段（包括 private）都可以被序列化/反序列化
        // 作用：不需要为字段编写 getter/setter 就能序列化
       // mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // 设置 getter 可见性：PUBLIC_ONLY - 只有 public 的 getter 被识别
        // 作用：优先使用 getter，但也保留字段访问作为备选
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);

        // 设置 setter 可见性：PUBLIC_ONLY - 只有 public 的 setter 被识别
        // 作用：优先使用 setter，但也保留字段访问作为备选
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);

        // 其他推荐配置

        // 禁用将日期写入为时间戳格式
        // 作用：日期改为 ISO 8601 字符串格式（如 "2024-01-15T10:30:00"）
        // 原因：时间戳可读性差，且容易出时区问题
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 禁用反序列化时遇到未知属性就失败的特性
        // 作用：JSON 中有 Java 对象不存在的字段时，忽略这些字段而不是抛出异常
        // 原因：提高兼容性，前端添加新字段不会导致后端报错
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 启用反序列化时将未知枚举值转为 null 的特性
        // 作用：JSON 中的枚举值不在 Java 枚举定义中时，反序列化为 null
        // 原因：避免因枚举值变更导致反序列化失败
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

        return mapper;
    }
}