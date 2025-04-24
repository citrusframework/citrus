package org.citrusframework.util;

import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.util.EnvUtils.booleanPropertyOrDefault;
import static org.citrusframework.util.EnvUtils.enumPropertyOrDefault;
import static org.citrusframework.util.EnvUtils.transformPropertyToEnv;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class EnvUtilsTest {

    private SystemProvider systemProvider;

    @BeforeMethod
    public void beforeMethod() {
        systemProvider = mock(SystemProvider.class);
    }

    @Test
    public void testTransformPropertyToEnv() {
        assertEquals(transformPropertyToEnv("foo.bar"), "FOO_BAR");
        assertEquals(transformPropertyToEnv("test.value"), "TEST_VALUE");
        assertEquals(transformPropertyToEnv("hello.world"), "HELLO_WORLD");
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
    public void testEnumPropertyOrDefault_PropertySet() {
        when(systemProvider.getProperty("enum.test")).thenReturn(Optional.of("VALUE_ONE"));

        TestEnum result = enumPropertyOrDefault(systemProvider, TestEnum.class, "enum.test",
            "ENUM_TEST", TestEnum.VALUE_TWO);
        assertEquals(result, TestEnum.VALUE_ONE);
    }

    @Test
    public void testEnumPropertyOrDefault_EnvSet() {
        when(systemProvider.getProperty("enum.test")).thenReturn(Optional.empty());
        when(systemProvider.getEnv("ENUM_TEST")).thenReturn(Optional.of("VALUE_TWO"));

        TestEnum result = enumPropertyOrDefault(systemProvider, TestEnum.class, "enum.test",
            "ENUM_TEST", TestEnum.VALUE_ONE);
        assertEquals(result, TestEnum.VALUE_TWO);
    }

    @Test
    public void testEnumPropertyOrDefault_DefaultUsed() {
        when(systemProvider.getProperty("enum.test")).thenReturn(Optional.empty());
        when(systemProvider.getEnv("ENUM_TEST")).thenReturn(Optional.empty());

        TestEnum result = enumPropertyOrDefault(systemProvider, TestEnum.class, "enum.test",
            "ENUM_TEST", TestEnum.VALUE_ONE);
        assertEquals(result, TestEnum.VALUE_ONE);
    }

    enum TestEnum {
        VALUE_ONE, VALUE_TWO
    }

}
