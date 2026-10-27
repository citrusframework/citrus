package org.citrusframework.util;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.citrusframework.util.EnvUtils.booleanPropertyOrDefault;
import static org.citrusframework.util.EnvUtils.enumPropertyOrDefault;
import static org.citrusframework.util.EnvUtils.transformPropertyToEnv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnvUtilsTest {

    private SystemProvider systemProvider;

    @BeforeEach
    public void beforeEach() {
        systemProvider = mock(SystemProvider.class);
    }

    @Test
    public void testTransformPropertyToEnv() {
        assertEquals("FOO_BAR", transformPropertyToEnv("foo.bar"));
        assertEquals("TEST_VALUE", transformPropertyToEnv("test.value"));
        assertEquals("HELLO_WORLD", transformPropertyToEnv("hello.world"));
    }

    @Test
    public void testBooleanPropertyOrDefault_PropertySet() {
        when(systemProvider.getProperty("feature.enabled")).thenReturn(Optional.of("true"));

        boolean result = booleanPropertyOrDefault(systemProvider, "feature.enabled",
            "FEATURE_ENABLED", false);
        assertTrue(result);
    }

    @Test
    public void testBooleanPropertyOrDefault_EnvSet() {
        when(systemProvider.getProperty("feature.enabled")).thenReturn(Optional.empty());
        when(systemProvider.getEnv("FEATURE_ENABLED")).thenReturn(Optional.of("false"));

        boolean result = booleanPropertyOrDefault(systemProvider, "feature.enabled",
            "FEATURE_ENABLED", true);
        assertFalse(result);
    }

    @Test
    public void testBooleanPropertyOrDefault_DefaultUsed() {
        when(systemProvider.getProperty("feature.enabled")).thenReturn(Optional.empty());
        when(systemProvider.getEnv("FEATURE_ENABLED")).thenReturn(Optional.empty());

        boolean result = booleanPropertyOrDefault(systemProvider, "feature.enabled",
            "FEATURE_ENABLED", true);
        assertTrue(result);
    }

    @Test
    public void testBooleanPropertyOrDefault_DefaultTrueOnEmptyProperty() {
        when(systemProvider.getProperty("feature.enabled")).thenReturn(Optional.of(""));
        when(systemProvider.getEnv("FEATURE_ENABLED")).thenReturn(Optional.empty());

        boolean result = booleanPropertyOrDefault(systemProvider, "feature.enabled",
            "FEATURE_ENABLED", true);
        assertTrue(result);
    }

    @Test
    public void testBooleanPropertyOrDefault_DefaultFalseOnEmptyProperty() {
        when(systemProvider.getProperty("feature.enabled")).thenReturn(Optional.of(""));
        when(systemProvider.getEnv("FEATURE_ENABLED")).thenReturn(Optional.empty());

        boolean result = booleanPropertyOrDefault(systemProvider, "feature.enabled",
            "FEATURE_ENABLED", false);
        assertFalse(result);
    }

    @Test
    public void testEnumPropertyOrDefault_PropertySet() {
        when(systemProvider.getProperty("enum.test")).thenReturn(Optional.of("VALUE_ONE"));

        TestEnum result = enumPropertyOrDefault(systemProvider, TestEnum.class, "enum.test",
            "ENUM_TEST", TestEnum.VALUE_TWO);
        assertEquals(TestEnum.VALUE_ONE, result);
    }

    @Test
    public void testEnumPropertyOrDefault_EnvSet() {
        when(systemProvider.getProperty("enum.test")).thenReturn(Optional.empty());
        when(systemProvider.getEnv("ENUM_TEST")).thenReturn(Optional.of("VALUE_TWO"));

        TestEnum result = enumPropertyOrDefault(systemProvider, TestEnum.class, "enum.test",
            "ENUM_TEST", TestEnum.VALUE_ONE);
        assertEquals(TestEnum.VALUE_TWO, result);
    }

    @Test
    public void testEnumPropertyOrDefault_DefaultUsed() {
        when(systemProvider.getProperty("enum.test")).thenReturn(Optional.empty());
        when(systemProvider.getEnv("ENUM_TEST")).thenReturn(Optional.empty());

        TestEnum result = enumPropertyOrDefault(systemProvider, TestEnum.class, "enum.test",
            "ENUM_TEST", TestEnum.VALUE_ONE);
        assertEquals(TestEnum.VALUE_ONE, result);
    }

    enum TestEnum {
        VALUE_ONE, VALUE_TWO
    }

}
