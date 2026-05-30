package com.rz.langchain.demo.server;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public final class JacksonHelper {
    public static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static String toJson(Object obj, boolean throwing) {
        if (null == obj) {
            return "";
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_SERIALIZE_JSON(throwable, obj.getClass().getName());
            }
            log.error("failed to serialize object({}) to json", obj.getClass().getName(), throwable);

            return "";
        }
    }

    public static String toPrettyJson(Object obj, boolean throwing) {
        if (null == obj) {
            return "";
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_SERIALIZE_JSON(throwable, obj.getClass().getName());
            }
            log.error("failed to serialize object({}) to json", obj.getClass().getName(), throwable);

            return "";
        }
    }

    public static byte[] toJsonBytes(Object obj, boolean throwing) {
        try {
            String json = mapper.writeValueAsString(obj);
            if (null == json) {
                return new byte[0];
            }
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_SERIALIZE_JSON(throwable, obj.getClass().getName());
            }
            log.warn("failed to serialize object({}) to byte", obj.getClass().getName(), throwable);

            return new byte[0];
        }
    }

    public static <T> T toObj(byte[] json, Class<T> cls, boolean throwing) {
        try {
            return toObj(new String(json, StandardCharsets.UTF_8), cls, throwing);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == cls ? "" : cls.getTypeName());
            }
            log.warn("failed to deserialize json to object({})", null == cls ? "" : cls.getTypeName(), throwable);

            return null;
        }
    }

    public static <T> T toObj(byte[] json, Type cls, boolean throwing) {
        try {
            return toObj(new String(json, StandardCharsets.UTF_8), cls, throwing);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == cls ? "" : cls.getTypeName());
            }
            log.warn("failed to deserialize json to object({})", null == cls ? "" : cls.getTypeName(), throwable);

            return null;
        }
    }

    public static <T> T toObj(InputStream inputStream, Class<T> cls, boolean throwing) {
        try {
            return mapper.readValue(inputStream, cls);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == cls ? "" : cls.getName());
            }
            log.warn("failed to deserialize json to object({})", null == cls ? "" : cls.getTypeName(), throwable);

            return null;
        }
    }

    public static <T> T toObj(byte[] json, TypeReference<T> typeReference, boolean throwing) {
        try {
            return toObj(new String(json, StandardCharsets.UTF_8), typeReference, throwing);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == typeReference || null == typeReference.getType() ? "" : typeReference.getType().getTypeName());
            }
            log.warn("failed to deserialize json to object({})", null == typeReference || null == typeReference.getType() ? "" : typeReference.getType().getTypeName(), throwable);

            return null;
        }
    }

    public static <T> T toObj(String json, Class<T> cls, boolean throwing) {
        try {
            return mapper.readValue(json, cls);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == cls ? "" : cls.getTypeName());
            }
            log.warn("failed to deserialize json to object({})", null == cls ? "" : cls.getTypeName(), throwable);

            return null;
        }
    }

    public static <T> T toObj(String json, Type type, boolean throwing) {
        try {
            return mapper.readValue(json, mapper.constructType(type));
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == type ? "" : type.getTypeName());
            }
            log.warn("failed to deserialize json to object({})", null == type ? "" : type.getTypeName(), throwable);

            return null;
        }
    }

    public static <T> T toObj(String json, TypeReference<T> typeReference, boolean throwing) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == typeReference ? "" : typeReference.getClass().getName());
            }
            log.warn("failed to deserialize json to object({})", null == typeReference ? "" : typeReference.getClass().getName(), throwable);

            return null;
        }
    }

    public static <T> T toObj(InputStream inputStream, Type type, boolean throwing) {
        try {
            return mapper.readValue(inputStream, mapper.constructType(type));
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, null == type ? "" : type.getTypeName());
            }
            log.warn("failed to deserialize json to object({})", null == type ? "" : type.getTypeName(), throwable);

            return null;
        }
    }

    public static JsonNode toObj(String json, boolean throwing) {
        try {
            return mapper.readTree(json);
        } catch (Throwable throwable) {
            if (throwing) {
                throw JacksonHelper.FAILED_DESERIALIZATION_JSON(throwable, JsonNode.class.getName());
            }
            log.warn("failed to deserialize json to object({})", JsonNode.class.getName(), throwable);

            return null;
        }
    }

    public static <T> List<T> toObjs(String json, boolean throwing) {
        return JacksonHelper.toObj(json, new TypeReference<List<T>>() {
        }, throwing);
    }

    public static void registerSubtype(Class<?> clz, String type) {
        mapper.registerSubtypes(new NamedType(clz, type));
    }

    public static ObjectNode createEmptyJsonNode() {
        return new ObjectNode(mapper.getNodeFactory());
    }

    public static ArrayNode createEmptyArrayNode() {
        return new ArrayNode(mapper.getNodeFactory());
    }

    public static JsonNode transferToJsonNode(Object obj) {
        return mapper.valueToTree(obj);
    }

    public static JavaType constructJavaType(Type type) {
        return mapper.constructType(type);
    }

    private static RuntimeException FAILED_DESERIALIZATION_JSON(Throwable throwable, String className) {
        return new RuntimeException(String.format("failed to deserialize json to object(%s)", className), throwable);
    }

    private static RuntimeException FAILED_SERIALIZE_JSON(Throwable throwable, String className) {
        return new RuntimeException(String.format("failed to serialize object(%s) to json", className), throwable);
    }
}
