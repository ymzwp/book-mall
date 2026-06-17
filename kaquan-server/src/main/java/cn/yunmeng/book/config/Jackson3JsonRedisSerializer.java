package cn.yunmeng.book.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/** Jackson 3.x 版 Redis JSON 序列化器（替代 GenericJackson2JsonRedisSerializer） */
public class Jackson3JsonRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper mapper;

    public Jackson3JsonRedisSerializer() {
        this.mapper = JsonMapper.builder()
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder().build(),
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();
    }

    @Override
    public byte[] serialize(Object source) throws SerializationException {
        if (source == null) return new byte[0];
        try {
            return mapper.writeValueAsBytes(source);
        } catch (Exception e) {
            throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] source) throws SerializationException {
        if (source == null || source.length == 0) return null;
        try {
            return mapper.readValue(source, Object.class);
        } catch (Exception e) {
            throw new SerializationException("Could not read JSON: " + e.getMessage(), e);
        }
    }
}
