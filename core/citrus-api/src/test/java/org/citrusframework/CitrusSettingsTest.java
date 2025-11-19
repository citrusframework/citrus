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
import static org.citrusframework.CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_ENV;
import static org.citrusframework.CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_PROPERTY;
import static org.citrusframework.CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV;
import static org.citrusframework.CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY;
import static org.citrusframework.validation.CustomValidatorStrategy.COMBINED;
import static org.citrusframework.validation.CustomValidatorStrategy.EXCLUSIVE;

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
}
