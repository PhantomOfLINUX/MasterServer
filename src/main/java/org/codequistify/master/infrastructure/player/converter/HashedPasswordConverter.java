package org.codequistify.master.infrastructure.player.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.codequistify.master.core.domain.player.model.HashedPassword;

@Converter(autoApply = true)
public class HashedPasswordConverter implements AttributeConverter<HashedPassword, String> {

    @Override
    public String convertToDatabaseColumn(HashedPassword attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public HashedPassword convertToEntityAttribute(String dbData) {
        return dbData != null ? HashedPassword.fromHashed(dbData) : null;
    }
}