/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.cucumber;

import java.util.ArrayList;
import java.util.List;

import io.cucumber.core.backend.discovery.UriGlueDiscoverySelector;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.Options;
import org.citrusframework.api.main.TestRunConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

public class CucumberTestEngineSuiteOptionsTest {

    private static final String ECHO_PACKAGE = "org.citrusframework.cucumber.integration.echo";

    @Test
    public void shouldParseSuiteOptions() {
        List<String> features = new ArrayList<>();

        CucumberTestEngine engine = new CucumberTestEngine(new TestRunConfiguration());
        RuntimeOptions options = engine.parseSuiteOptions(SuiteConfiguration.class, propertiesFileOptions(), features);

        Assert.assertEquals(features, List.of("classpath:" + ECHO_PACKAGE.replace('.', '/')));

        Assert.assertTrue(options.getGlueDiscoveryRequest().getSelectorsByType(UriGlueDiscoverySelector.class).stream()
                .map(UriGlueDiscoverySelector::uri)
                .anyMatch(uri -> glueUri(ECHO_PACKAGE).equals(uri.toString())));

        List<String> plugins = options.plugins().stream()
                .map(Options.Plugin::pluginString)
                .toList();
        Assert.assertTrue(plugins.contains("pretty"));
        Assert.assertTrue(plugins.contains("org.citrusframework.cucumber.CitrusReporter"));
    }

    @Test
    public void shouldUseTestPackageAsDefaultGlue() {
        List<String> features = new ArrayList<>();

        CucumberTestEngine engine = new CucumberTestEngine(new TestRunConfiguration());
        RuntimeOptions options = engine.parseSuiteOptions(DefaultGlueSuiteConfiguration.class, propertiesFileOptions(), features);

        Assert.assertTrue(options.getGlueDiscoveryRequest().getSelectorsByType(UriGlueDiscoverySelector.class).stream()
                .map(UriGlueDiscoverySelector::uri)
                .anyMatch(uri -> glueUri(getClass().getPackageName()).equals(uri.toString())));
    }

    private static String glueUri(String packageName) {
        return "classpath:/" + packageName.replace('.', '/');
    }

    private static RuntimeOptions propertiesFileOptions() {
        return new CucumberPropertiesParser().parse(CucumberProperties.fromPropertiesFile()).build();
    }

    @Suite
    @SelectPackages(ECHO_PACKAGE)
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = ECHO_PACKAGE)
    @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,org.citrusframework.cucumber.CitrusReporter")
    public static class SuiteConfiguration {
    }

    @Suite
    @SelectPackages(ECHO_PACKAGE)
    public static class DefaultGlueSuiteConfiguration {
    }
}
