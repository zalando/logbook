package org.zalando.logbook.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingException;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.Generated;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * A temporary workaround to support Jackson 3.x (tools.jackson namespace) until there is a supported version in json-path.
 * This class is a copy of com.jayway.jsonpath.spi.mapper.JacksonMappingProvider with imports and minor adjustments to work with Jackson 3.x.
**/
// A hack to not have JaCoCo complain about missing test coverage as this is just a copy of an existing class with minor changes
@Generated
final class LogbookJacksonMappingProvider implements MappingProvider {

    private final JsonMapper jsonMapper;

    public LogbookJacksonMappingProvider() {
        this(new JsonMapper());
    }

    public LogbookJacksonMappingProvider(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public <T> T map(Object source, Class<T> targetType, Configuration configuration) {
        if (source == null) {
            return null;
        } else {
            try {
                return (T)this.jsonMapper.convertValue(source, targetType);
            } catch (Exception e) {
                throw new MappingException(e);
            }
        }
    }

    public <T> T map(Object source, TypeRef<T> targetType, Configuration configuration) {
        if (source == null) {
            return null;
        } else {
            JavaType type = this.jsonMapper.getTypeFactory().constructType(targetType.getType());

            try {
                return (T)this.jsonMapper.convertValue(source, type);
            } catch (Exception e) {
                throw new MappingException(e);
            }
        }
    }
}
