package com.company.game_engine_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BoardConverter implements AttributeConverter<String[][], String> {

    private final ObjectMapper objectMapper;

    public BoardConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(String[][] attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize board array to JSON string", e);
        }
    }

    @Override
    public String[][] convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, String[][].class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not deserialize JSON string back to board array", e);
        }
    }
}
