package org.citrusframework;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.properties.SystemProperties;
import uk.org.webcompere.systemstubs.testng.SystemStub;
import uk.org.webcompere.systemstubs.testng.SystemStubsListener;

import static org.citrusframework.CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV;
import static org.citrusframework.CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(SystemStubsListener.class)
public class CitrusSettingsTest {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @SystemStub
    private SystemProperties systemProperties;

    @BeforeMethod
    void beforeMethodSetup() {
        environmentVariables.remove(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV);
        systemProperties.remove(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY);
    }

    @Test
    public void isStackTraceOutputEnabled_shouldReturnFalseByDefault() {
        assertFalse(CitrusSettings.isStackTraceOutputEnabled());
    }

    @Test
    public void isStackTraceOutputEnabled_shouldReturnEnvVarValue() {
        environmentVariables.set(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV, "true");

        assertTrue(CitrusSettings.isStackTraceOutputEnabled());
    }

    @Test
    public void isStackTraceOutputEnabled_shouldReturnPropertyValue_overEnvVarValue() {
        systemProperties.set(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY, "true");

        // disregarded due to resolving sequence
        environmentVariables.set(DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV, "false");

        assertTrue(CitrusSettings.isStackTraceOutputEnabled());
    }
}
