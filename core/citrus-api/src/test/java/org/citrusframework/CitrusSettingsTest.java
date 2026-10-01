package org.citrusframework;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.properties.SystemProperties;
import uk.org.webcompere.systemstubs.testng.SystemStub;
import uk.org.webcompere.systemstubs.testng.SystemStubsListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.CitrusSettings.CITRUS_FILE_ENCODING_DEFAULT;
import static org.citrusframework.CitrusSettings.CITRUS_FILE_ENCODING_ENV;
import static org.citrusframework.CitrusSettings.CITRUS_FILE_ENCODING_PROPERTY;
import static org.citrusframework.CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_ENV;
import static org.citrusframework.CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_PROPERTY;
import static org.citrusframework.CitrusSettings.DEFAULT_CONFIG_CLASS_ENV;
import static org.citrusframework.CitrusSettings.DEFAULT_CONFIG_CLASS_PROPERTY;
import static org.citrusframework.CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV;
import static org.citrusframework.CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY;
import static org.citrusframework.CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY_DEFAULT;
import static org.citrusframework.CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY_ENV;
import static org.citrusframework.CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY_PROPERTY;
import static org.citrusframework.CitrusSettings.ENV_VAR_PROPERTY_BINDING_ENABLED_ENV;
import static org.citrusframework.CitrusSettings.ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY;
import static org.citrusframework.CitrusSettings.TEST_NAME_VARIABLE_DEFAULT;
import static org.citrusframework.CitrusSettings.TEST_NAME_VARIABLE_ENV;
import static org.citrusframework.CitrusSettings.TEST_NAME_VARIABLE_PROPERTY;
import static org.citrusframework.CitrusSettings.TEST_PACKAGE_VARIABLE_DEFAULT;
import static org.citrusframework.CitrusSettings.TEST_PACKAGE_VARIABLE_ENV;
import static org.citrusframework.CitrusSettings.TEST_PACKAGE_VARIABLE_PROPERTY;
import static org.citrusframework.validation.CustomValidatorStrategy.COMBINED;
import static org.citrusframework.validation.CustomValidatorStrategy.EXCLUSIVE;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(SystemStubsListener.class)
public class CitrusSettingsTest {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @SystemStub
    private SystemProperties systemProperties;

    @Test
    public void isStackTraceOutputEnabled_shouldReturnFalseByDefault() {
        systemProperties.remove(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY);
        environmentVariables.remove(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV);

        assertThat(CitrusSettings.isStackTraceOutputEnabled())
                .isFalse();
    }

    @Test
    public void isStackTraceOutputEnabled_shouldReturnEnvVarValue() {
        systemProperties.remove(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY);
        environmentVariables.set(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV, "true");

        assertThat(CitrusSettings.isStackTraceOutputEnabled())
                .isTrue();
    }

    @Test
    public void isStackTraceOutputEnabled_shouldReturnPropertyValue_overEnvVarValue() {
        systemProperties.set(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY, "true");

        // disregarded due to resolving sequence
        environmentVariables.set(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV, "false");

        assertThat(CitrusSettings.isStackTraceOutputEnabled())
                .isTrue();
    }

    @Test
    public void getCustomValidatorStrategy_shouldReturnExclusiveByDefault() {
        systemProperties.remove(CUSTOM_VALIDATOR_STRATEGY_PROPERTY);
        environmentVariables.remove(CUSTOM_VALIDATOR_STRATEGY_ENV);

        assertThat(CitrusSettings.getCustomValidatorStrategy())
                .isEqualTo(EXCLUSIVE);
    }

    @DataProvider
    public static String[] combinedPropertyValues() {
        return new String[]{
                "combined",
                "COMBINED"
        };
    }

    @Test(dataProvider = "combinedPropertyValues")
    public void getCustomValidatorStrategy_shouldReturnEnvVarValue(String combinedPropertyValue) {
        systemProperties.remove(CUSTOM_VALIDATOR_STRATEGY_PROPERTY);
        environmentVariables.set(CUSTOM_VALIDATOR_STRATEGY_ENV, combinedPropertyValue);

        assertThat(CitrusSettings.getCustomValidatorStrategy())
                .isEqualTo(COMBINED);
    }

    @Test(dataProvider = "combinedPropertyValues")
    public void getCustomValidatorStrategy_shouldReturnPropertyValue_overEnvVarValue(String combinedPropertyValue) {
        systemProperties.set(CUSTOM_VALIDATOR_STRATEGY_PROPERTY, combinedPropertyValue);

        // disregarded due to resolving sequence
        environmentVariables.set(CUSTOM_VALIDATOR_STRATEGY_ENV, "invalid");

        assertThat(CitrusSettings.getCustomValidatorStrategy())
                .isEqualTo(COMBINED);
    }

    @Test
    public void getCustomValidatorStrategy_shouldThrow_onInvalidProperty() {
        systemProperties.set(CUSTOM_VALIDATOR_STRATEGY_PROPERTY, "invalid");
        environmentVariables.remove(CUSTOM_VALIDATOR_STRATEGY_ENV);

        assertThatThrownBy(CitrusSettings::getCustomValidatorStrategy)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("No enum constant");
    }

    @Test
    public void getCustomValidatorStrategy_shouldThrow_OnInvalidEnv() {
        systemProperties.remove(CUSTOM_VALIDATOR_STRATEGY_PROPERTY);
        environmentVariables.set(CUSTOM_VALIDATOR_STRATEGY_ENV, "invalid");

        assertThatThrownBy(CitrusSettings::getCustomValidatorStrategy)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("No enum constant");
    }

    @Test
    public void getTestNameVariable_shouldReturnDefaultValue() {
        systemProperties.remove(TEST_NAME_VARIABLE_PROPERTY);
        environmentVariables.remove(TEST_NAME_VARIABLE_ENV);

        assertThat(CitrusSettings.getTestNameVariable())
                .isEqualTo(TEST_NAME_VARIABLE_DEFAULT);
    }

    @Test
    public void getTestNameVariable_shouldReturnPropertyValue() {
        systemProperties.set(TEST_NAME_VARIABLE_PROPERTY, "custom.test.name");

        assertThat(CitrusSettings.getTestNameVariable())
                .isEqualTo("custom.test.name");
    }

    @Test
    public void getTestNameVariable_shouldReturnEnvVarValue() {
        systemProperties.remove(TEST_NAME_VARIABLE_PROPERTY);
        environmentVariables.set(TEST_NAME_VARIABLE_ENV, "env.test.name");

        assertThat(CitrusSettings.getTestNameVariable())
                .isEqualTo("env.test.name");
    }

    @Test
    public void getTestPackageVariable_shouldReturnDefaultValue() {
        systemProperties.remove(TEST_PACKAGE_VARIABLE_PROPERTY);
        environmentVariables.remove(TEST_PACKAGE_VARIABLE_ENV);

        assertThat(CitrusSettings.getTestPackageVariable())
                .isEqualTo(TEST_PACKAGE_VARIABLE_DEFAULT);
    }

    @Test
    public void getTestPackageVariable_shouldReturnPropertyValue() {
        systemProperties.set(TEST_PACKAGE_VARIABLE_PROPERTY, "custom.test.package");

        assertThat(CitrusSettings.getTestPackageVariable())
                .isEqualTo("custom.test.package");
    }

    @Test
    public void getFileEncoding_shouldReturnDefaultValue() {
        systemProperties.remove(CITRUS_FILE_ENCODING_PROPERTY);
        environmentVariables.remove(CITRUS_FILE_ENCODING_ENV);

        assertThat(CitrusSettings.getFileEncoding())
                .isEqualTo(CITRUS_FILE_ENCODING_DEFAULT);
    }

    @Test
    public void getFileEncoding_shouldReturnPropertyValue() {
        systemProperties.set(CITRUS_FILE_ENCODING_PROPERTY, "ISO-8859-1");

        assertThat(CitrusSettings.getFileEncoding())
                .isEqualTo("ISO-8859-1");
    }

    @Test
    public void getDefaultConfigClass_shouldReturnNullByDefault() {
        systemProperties.remove(DEFAULT_CONFIG_CLASS_PROPERTY);
        environmentVariables.remove(DEFAULT_CONFIG_CLASS_ENV);

        assertThat(CitrusSettings.getDefaultConfigClass())
                .isNull();
    }

    @Test
    public void getDefaultConfigClass_shouldReturnPropertyValue() {
        systemProperties.set(DEFAULT_CONFIG_CLASS_PROPERTY, "com.example.TestConfig");

        assertThat(CitrusSettings.getDefaultConfigClass())
                .isEqualTo("com.example.TestConfig");
    }

    @Test
    public void getDefaultTestSrcDirectory_shouldReturnDefaultValue() {
        systemProperties.remove(DEFAULT_TEST_SRC_DIRECTORY_PROPERTY);
        environmentVariables.remove(DEFAULT_TEST_SRC_DIRECTORY_ENV);

        assertThat(CitrusSettings.getDefaultTestSrcDirectory())
                .isEqualTo(DEFAULT_TEST_SRC_DIRECTORY_DEFAULT);
    }

    @Test
    public void getDefaultTestSrcDirectory_shouldReturnPropertyValue() {
        systemProperties.set(DEFAULT_TEST_SRC_DIRECTORY_PROPERTY, "src/it/");

        assertThat(CitrusSettings.getDefaultTestSrcDirectory())
                .isEqualTo("src/it/");
    }

    @Test
    public void isComponentPropertyBindingEnabled_shouldReturnTrueByDefault() {
        systemProperties.remove(ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY);
        environmentVariables.remove(ENV_VAR_PROPERTY_BINDING_ENABLED_ENV);

        assertTrue(CitrusSettings.isComponentPropertyBindingEnabled());
    }

    @Test
    public void isComponentPropertyBindingEnabled_shouldReturnEnvVarPropertyBindingValue() {
        systemProperties.set(ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY, "false");

        assertFalse(CitrusSettings.isComponentPropertyBindingEnabled());
    }

    @Test
    public void isEndpointPropertyBindingEnabled_shouldReturnTrueByDefault() {
        systemProperties.remove(ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY);
        environmentVariables.remove(ENV_VAR_PROPERTY_BINDING_ENABLED_ENV);

        assertTrue(CitrusSettings.isEndpointPropertyBindingEnabled());
    }

    @Test
    public void isEndpointPropertyBindingEnabled_shouldReturnEnvVarPropertyBindingValue() {
        systemProperties.set(ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY, "false");

        assertFalse(CitrusSettings.isEndpointPropertyBindingEnabled());
    }
}
