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
 * Class calling Camel JBang Kubernetes plugin operations
 */
public class KubernetesPlugin {

    private final JBangSupport camelApp;

    public KubernetesPlugin(JBangSupport camelApp) {
        this.camelApp = camelApp;
    }

    /**
     * Run given integration on Kubernetes with JBang Camel.
     */
    public ProcessAndOutput run(String fileName, String... args) {
        List<String> runArgs = new ArrayList<>();

        runArgs.add("run");
        runArgs.add(fileName);
        runArgs.addAll(Arrays.asList(args));

        return camelApp.run("kubernetes", runArgs.toArray(String[]::new));
    }

    /**
     * Remove given integration from Kubernetes with Camel JBang.
     */
    public ProcessAndOutput delete(String fileName, String... args) {
        List<String> commandArgs = new ArrayList<>();

        commandArgs.add("delete");
        commandArgs.add(fileName);
        commandArgs.addAll(Arrays.asList(args));

        return camelApp.run("kubernetes", commandArgs.toArray(String[]::new));
    }

    /**
     * Provides logs from given integration running on Kubernetes.
     */
    public ProcessAndOutput logs(String... args) {
        List<String> commandArgs = new ArrayList<>();

        commandArgs.add("logs");
        commandArgs.addAll(Arrays.asList(args));

        return camelApp.runAsync("kubernetes", commandArgs.toArray(String[]::new));
    }
}
