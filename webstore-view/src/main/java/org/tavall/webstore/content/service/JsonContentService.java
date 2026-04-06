package org.tavall.webstore.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JsonContentService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Map<String, Object>>> MAP_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public JsonContentService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> parseObject(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return Collections.emptyMap();
        }
        return readValue(rawJson, MAP_TYPE);
    }

    public List<Map<String, Object>> parseObjectList(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return Collections.emptyList();
        }
        return readValue(rawJson, MAP_LIST_TYPE);
    }

    public List<String> parseStringList(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return Collections.emptyList();
        }
        return readValue(rawJson, STRING_LIST_TYPE);
    }

    public String writeJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to write JSON value.", exception);
        }
    }

    private <T> T readValue(String rawJson, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(rawJson, typeReference);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid JSON content supplied.", exception);
        }
    }
}
