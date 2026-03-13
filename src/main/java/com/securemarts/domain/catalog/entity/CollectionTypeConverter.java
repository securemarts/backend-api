package com.securemarts.domain.catalog.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts between DB string (lowercase 'manual'/'smart') and Collection.CollectionType enum.
 */
@Converter(autoApply = true)
public class CollectionTypeConverter implements AttributeConverter<Collection.CollectionType, String> {

    @Override
    public String convertToDatabaseColumn(Collection.CollectionType attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public Collection.CollectionType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return Collection.CollectionType.MANUAL;
        return switch (dbData.trim().toLowerCase()) {
            case "smart" -> Collection.CollectionType.SMART;
            default -> Collection.CollectionType.MANUAL;
        };
    }
}
