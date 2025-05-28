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

package org.citrusframework.camel.jbang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessAndOutput;

/**
 * Class calling Camel JBang infrastructure plugin operations
 */
public class InfraPlugin {

    private final JBangSupport camelApp;

    public InfraPlugin(JBangSupport camelApp) {
        this.camelApp = camelApp;
    }

    /**
     * Run infrastructure with JBang Camel.
     */
    public ProcessAndOutput run(String serviceName, String... args) {
        List<String> runArgs = new ArrayList<>();

        runArgs.add("run");
        runArgs.add(serviceName);
        runArgs.addAll(Arrays.asList(args));

        return camelApp.run("infra", runArgs.toArray(String[]::new));
    }

    /**
     * Remove given infrastructure instance with Camel JBang.
     */
    public ProcessAndOutput delete(String serviceName, String... args) {
        List<String> commandArgs = new ArrayList<>();

        commandArgs.add("stop");
        commandArgs.add(serviceName);
        commandArgs.addAll(Arrays.asList(args));

        return camelApp.run("infra", commandArgs.toArray(String[]::new));
    }

    /**
     * Provides logs from given infrastructure service running with Camel JBang.
     */
    public ProcessAndOutput logs(String serviceName, String... args) {
        List<String> commandArgs = new ArrayList<>();

        commandArgs.add("log");
        commandArgs.add(serviceName);
        commandArgs.addAll(Arrays.asList(args));

        return camelApp.runAsync("infra", commandArgs.toArray(String[]::new));
    }
}
