package org.codequistify.master.infrastructure.player.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.codequistify.master.core.domain.player.model.PolId;

@Converter(autoApply = true)
public class PolIdConverter implements AttributeConverter<PolId, String> {
    @Override
    public String convertToDatabaseColumn(PolId attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PolId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PolId.of(dbData);
    }
}
