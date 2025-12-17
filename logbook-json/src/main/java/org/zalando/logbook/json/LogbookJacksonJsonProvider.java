package org.zalando.logbook.json;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;
import lombok.Generated;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A temporary workaround to support Jackson 3.x (tools.jackson namespace) until there is a supported version in json-path.
 * This class is a copy of com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider with imports and minor adjustments to work with Jackson 3.x.
 **/
// A hack to not have JaCoCo complain about missing test coverage as this is just a copy of an existing class with minor changes
@Generated
final class LogbookJacksonJsonProvider extends AbstractJsonProvider {
    private static final JsonMapper DEFAULT_MAPPER = new JsonMapper();
    private final JsonMapper jsonMapper;

    public LogbookJacksonJsonProvider() {
        this(DEFAULT_MAPPER);
    }

    public LogbookJacksonJsonProvider(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public Object parse(String json) throws InvalidJsonException {
        try {
            return this.jsonMapper.readTree(json);
        } catch (JacksonException e) {
            throw new InvalidJsonException(e, json);
        }
    }

    public Object parse(byte[] json) throws InvalidJsonException {
        try {
            return this.jsonMapper.readTree(json);
        } catch (JacksonException e) {
            throw new InvalidJsonException(e, new String(json, StandardCharsets.UTF_8));
        }
    }

    @Override
    public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
        try {
            return this.jsonMapper.readTree(new InputStreamReader(jsonStream, charset));
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public String toJson(Object obj) {
        if (!(obj instanceof JsonNode)) {
            throw new JsonPathException("Not a JSON Node");
        } else {
            return obj.toString();
        }
    }

    @Override
    public Object createArray() {
        return JsonNodeFactory.instance.arrayNode();
    }

    @Override
    public Object createMap() {
        return JsonNodeFactory.instance.objectNode();
    }

    public Object unwrap(Object o) {
        if (o == null) {
            return null;
        } else if (!(o instanceof JsonNode)) {
            return o;
        } else {
            JsonNode e = (JsonNode) o;
            if (e.isValueNode()) {
                if (e.isString()) {
                    return e.asString();
                }

                if (e.isBoolean()) {
                    return e.asBoolean();
                }

                if (e.isInt()) {
                    return e.asInt();
                }

                if (e.isLong()) {
                    return e.asLong();
                }

                if (e.isBigInteger()) {
                    return e.bigIntegerValue();
                }

                if (e.isDouble()) {
                    return e.doubleValue();
                }

                if (e.isFloat()) {
                    return e.floatValue();
                }

                if (e.isBigDecimal()) {
                    return e.decimalValue();
                }

                if (e.isNull()) {
                    return null;
                }
            }

            return o;
        }
    }

    public boolean isArray(Object obj) {
        return obj instanceof ArrayNode || obj instanceof List;
    }

    public Object getArrayIndex(Object obj, int idx) {
        return this.toJsonArray(obj).get(idx);
    }

    public void setArrayIndex(Object array, int index, Object newValue) {
        if (!this.isArray(array)) {
            throw new UnsupportedOperationException();
        } else {
            ArrayNode arrayNode = this.toJsonArray(array);
            if (index == arrayNode.size()) {
                arrayNode.add(this.createJsonElement(newValue));
            } else {
                arrayNode.set(index, this.createJsonElement(newValue));
            }

        }
    }

    public Object getMapValue(Object obj, String key) {
        ObjectNode jsonObject = this.toJsonObject(obj);
        Object o = jsonObject.get(key);
        return !jsonObject.has(key) ? UNDEFINED : o;
    }

    public void setProperty(Object obj, Object key, Object value) {
        if (this.isMap(obj)) {
            this.setValueInObjectNode((ObjectNode) obj, key, value);
        } else {
            ArrayNode array = (ArrayNode) obj;
            int index;
            if (key != null) {
                index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            } else {
                index = array.size();
            }

            if (index == array.size()) {
                array.add(this.createJsonElement(value));
            } else {
                array.set(index, this.createJsonElement(value));
            }
        }

    }

    public void removeProperty(Object obj, Object key) {
        if (this.isMap(obj)) {
            this.toJsonObject(obj).remove(key.toString());
        } else {
            ArrayNode array = this.toJsonArray(obj);
            int index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            array.remove(index);
        }

    }

    public boolean isMap(Object obj) {
        return obj instanceof ObjectNode;
    }

    public Collection<String> getPropertyKeys(Object obj) {
        return this.toJsonObject(obj).propertyNames();
    }

    public int length(Object obj) {
        if (this.isArray(obj)) {
            return this.toJsonArray(obj).size();
        } else if (this.isMap(obj)) {
            return this.toJsonObject(obj).size();
        } else if (obj instanceof StringNode element) {
            return element.size();
        } else {
            throw new JsonPathException("length operation can not applied to " + (obj != null ? obj.getClass().getName() : "null"));
        }
    }

    public Iterable<?> toIterable(Object obj) {
        ArrayNode arr = this.toJsonArray(obj);
        final Iterator<?> iterator = arr.iterator();
        return new Iterable<Object>() {
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public Object next() {
                        return unwrap(iterator.next());
                    }
                };
            }
        };
    }

    private JsonNode createJsonElement(Object o) {
        if (o != null) {
            return o instanceof JsonNode ? (JsonNode) o : this.jsonMapper.valueToTree(o);
        } else {
            return null;
        }
    }

    private ArrayNode toJsonArray(Object o) {
        return (ArrayNode) o;
    }

    private ObjectNode toJsonObject(Object o) {
        return (ObjectNode) o;
    }

    private void setValueInObjectNode(ObjectNode objectNode, Object key, Object value) {
        if (value instanceof JsonNode) {
            objectNode.set(key.toString(), (JsonNode) value);
        } else if (value instanceof String) {
            objectNode.put(key.toString(), (String) value);
        } else if (value instanceof Integer) {
            objectNode.put(key.toString(), (Integer) value);
        } else if (value instanceof Long) {
            objectNode.put(key.toString(), (Long) value);
        } else if (value instanceof Short) {
            objectNode.put(key.toString(), (Short) value);
        } else if (value instanceof BigInteger) {
            objectNode.put(key.toString(), (BigInteger) value);
        } else if (value instanceof Double) {
            objectNode.put(key.toString(), (Double) value);
        } else if (value instanceof Float) {
            objectNode.put(key.toString(), (Float) value);
        } else if (value instanceof BigDecimal) {
            objectNode.put(key.toString(), (BigDecimal) value);
        } else if (value instanceof Boolean) {
            objectNode.put(key.toString(), (Boolean) value);
        } else if (value instanceof byte[]) {
            objectNode.put(key.toString(), (byte[]) value);
        } else if (value == null) {
            objectNode.set(key.toString(), (JsonNode) null);
        } else {
            objectNode.set(key.toString(), this.createJsonElement(value));
        }
    }
}
