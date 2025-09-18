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

package org.citrusframework.cucumber.hooks;

import java.util.Optional;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.CucumberSettings;
import org.citrusframework.cucumber.VariableNames;
import org.citrusframework.cucumber.util.FeatureHelper;

/**
 * Cucumber hook injects environment variables as test variables before the scenario is executed.
 */
public class InjectEnvVarsHook {

    @CitrusResource
    private TestCaseRunner runner;

    @Before(order = Integer.MAX_VALUE)
    public void injectEnvVars(Scenario scenario) {
        runner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                if (scenario != null) {
                    context.setVariable(VariableNames.FEATURE_FILE.value(), FeatureHelper.extractFeatureFile(scenario));
                    context.setVariable(VariableNames.FEATURE_FILENAME.value(), FeatureHelper.extractFeatureFileName(scenario));
                    context.setVariable(VariableNames.FEATURE_PACKAGE.value(), FeatureHelper.extractFeaturePackage(scenario));

                    context.setVariable(VariableNames.SCENARIO_ID.value(), scenario.getId());
                    context.setVariable(VariableNames.SCENARIO_NAME.value(), scenario.getName());

                    scenario.getUri();
                }

                Optional<String> namespaceEnv = getNamespaceSetting();
                Optional<String> domainEnv = getClusterWildcardSetting();

                if (namespaceEnv.isPresent()) {
                    context.setVariable(VariableNames.NAMESPACE.value(), namespaceEnv.get());

                    if (domainEnv.isEmpty()) {
                        context.setVariable(VariableNames.CLUSTER_WILDCARD_DOMAIN.value(), namespaceEnv.get() + "." + CucumberSettings.DEFAULT_DOMAIN_SUFFIX);
                    }
                }

                domainEnv.ifPresent(var -> context.setVariable(VariableNames.CLUSTER_WILDCARD_DOMAIN.value(), var));

                context.setVariable(VariableNames.OPERATOR_NAMESPACE.value(), CucumberSettings.getOperatorNamespace());
            }
        });
    }

    protected Optional<String> getClusterWildcardSetting() {
        return Optional.ofNullable(CucumberSettings.getClusterWildcardDomain());
    }

    protected Optional<String> getNamespaceSetting() {
        return Optional.ofNullable(CucumberSettings.getDefaultNamespace());
    }
}
