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

import org.assertj.core.api.Assertions;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.cucumber.CucumberSettings;
import org.citrusframework.cucumber.VariableNames;
import org.junit.Test;

public class InjectEnvVarsHookTest {

    @Test
    @SuppressWarnings("CucumberJavaStepDefClassIsPublic")
    public void shouldInjectEnvVars() {
        InjectEnvVarsHook hook = new InjectEnvVarsHook() {
            @Override
            protected Optional<String> getNamespaceSetting() {
                return Optional.of("foo");
            }

            @Override
            protected Optional<String> getClusterWildcardSetting() {
                return Optional.of("foo.cluster.io");
            }
        };

        TestContext context = TestContextFactory.newInstance().getObject();
        TestCaseRunner runner = new DefaultTestCaseRunner(context);
        CitrusAnnotations.injectTestRunner(hook, runner);

        hook.injectEnvVars(null);

        Assertions.assertThat(context.getVariable(VariableNames.NAMESPACE.value())).isEqualTo("foo");
        Assertions.assertThat(context.getVariable(VariableNames.CLUSTER_WILDCARD_DOMAIN.value())).isEqualTo("foo.cluster.io");
    }

    @Test
    @SuppressWarnings("CucumberJavaStepDefClassIsPublic")
    public void shouldInjectEnvVarsDefaultValues() {
        InjectEnvVarsHook hook = new InjectEnvVarsHook() {
            @Override
            protected Optional<String> getNamespaceSetting() {
                return Optional.of("foo");
            }

            @Override
            protected Optional<String> getClusterWildcardSetting() {
                return Optional.empty();
            }
        };

        TestContext context = TestContextFactory.newInstance().getObject();
        TestCaseRunner runner = new DefaultTestCaseRunner(context);
        CitrusAnnotations.injectTestRunner(hook, runner);

        hook.injectEnvVars(null);

        Assertions.assertThat(context.getVariable(VariableNames.NAMESPACE.value())).isEqualTo("foo");
        Assertions.assertThat(context.getVariable(VariableNames.CLUSTER_WILDCARD_DOMAIN.value())).isEqualTo("foo." + CucumberSettings.DEFAULT_DOMAIN_SUFFIX);
    }
}
