package com.wdcftgg.witherstormmod.common.init;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModPaintingsTest {

    @Test
    void resolvesMcpPaintingFieldInDevelopment() throws Exception {
        Field field = ModPaintings.findField(
                DevelopmentNamedFields.class, "title", "field_75702_A");

        assertEquals("title", field.getName());
        assertEquals(String.class, field.getType());
    }

    @Test
    void fallsBackToSrgFieldName() throws Exception {
        Field field = ModPaintings.findField(
                RuntimeNamedFields.class, "sizeX", "field_75703_B");

        assertEquals("field_75703_B", field.getName());
    }

    @Test
    void reportsAllAttemptedNamesWhenNoFieldMatches() {
        NoSuchFieldException exception = assertThrows(
                NoSuchFieldException.class,
                () -> ModPaintings.findField(
                        RuntimeNamedFields.class, "sizeY", "field_75704_C"));

        assertEquals(
                "Unable to find [sizeY, field_75704_C] in "
                        + RuntimeNamedFields.class.getName(),
                exception.getMessage());
    }

    private static final class DevelopmentNamedFields {
        @SuppressWarnings("unused")
        private String title;
    }

    private static final class RuntimeNamedFields {
        @SuppressWarnings("unused")
        private int field_75703_B;
    }
}
