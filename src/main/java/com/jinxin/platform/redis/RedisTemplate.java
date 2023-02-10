package com.jinxin.platform.redis;

import com.jinxin.platform.utils.YamlConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Objects;

/**
 * redis工具类
 *
 * @author zengd
 * @version 1.0
 * @date 2022/9/19 15:49
 */
@Slf4j
public class RedisTemplate {
    private static final RedisSerializer<Object> SERIALIZER = new JdkSerializationRedisSerializer(RedisTemplate.class.getClassLoader());
    static RedisCommands<byte[], byte[]> syncCommands;

    static {
        String redisPw = YamlConfig.getValue("redis.password");
        String redisHost = YamlConfig.getValue("redis.host");
        String redisDatabase = YamlConfig.getValue("redis.database");
        String uri = String.format("redis://%s@%s:6379/%s", redisPw, redisHost, redisDatabase);

        RedisClient redisClient = RedisClient.create(uri);
        StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(new ByteArrayCodec());
        syncCommands = connection.sync();
        log.info("===>redis[{}]连接成功", redisHost);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            connection.close();
            redisClient.shutdown();
            log.info("===>redis[{}]断开连接", redisHost);
        }));
    }

    public static String get(String key) {
        byte[] bytes = syncCommands.get(SERIALIZER.serialize(key));
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        return (String) Objects.requireNonNull(SERIALIZER.deserialize(bytes));
    }

    public static void set(String key, Object obj) {
        byte[] values = SERIALIZER.serialize(obj);
        byte[] keys = SERIALIZER.serialize(key);
        syncCommands.set(keys, values);
    }

    public static void setEx(String key, long seconds, Object value) {
        byte[] values = SERIALIZER.serialize(value);
        byte[] keys = SERIALIZER.serialize(key);
        syncCommands.setex(keys, seconds, values);
    }

    public static void lpush(String key, Object obj) {
        byte[] values = SERIALIZER.serialize(obj);
        byte[] keys = SERIALIZER.serialize(key);
        syncCommands.lpush(keys, values);
    }

    public static void lrem(String key, Object obj) {
        byte[] values = SERIALIZER.serialize(obj);
        byte[] keys = SERIALIZER.serialize(key);
        syncCommands.lrem(keys, 0, values);
    }
}
