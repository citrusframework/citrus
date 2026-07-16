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

package org.citrusframework.camel.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessAndOutput;

/**
 * CamelCliLauncher implementation that delegates to JBang for executing Camel CLI commands.
 * Commands are constructed as: {@code jbang [-Dsysprop=val]... camel@apache/camel <command> <args>}
 */
public class JBangCamelLauncher implements CamelCliLauncher {

    private final JBangSupport delegate;

    public JBangCamelLauncher(String camelApp) {
        this.delegate = JBangSupport.jbang().app(camelApp);
    }

    public JBangCamelLauncher trust(String url) {
        delegate.trust(url);
        return this;
    }

    @Override
    public ProcessAndOutput run(String command, String... args) {
        return delegate.run(command, args);
    }

    @Override
    public ProcessAndOutput run(String command, List<String> args) {
        return delegate.run(command, args);
    }

    @Override
    public ProcessAndOutput runAsync(String command, String... args) {
        return delegate.runAsync(command, args);
    }

    @Override
    public ProcessAndOutput runAsync(String command, List<String> args) {
        return delegate.runAsync(command, args);
    }

    @Override
    public ProcessAndOutput runAsync(String command, File output, String... args) {
        return delegate.runAsync(command, output, args);
    }

    @Override
    public ProcessAndOutput runAsync(String command, File output, List<String> args) {
        return delegate.runAsync(command, output, args);
    }

    @Override
    public CamelCliLauncher withSystemProperty(String name, String value) {
        delegate.withSystemProperty(name, value);
        return this;
    }

    @Override
    public CamelCliLauncher withSystemProperties(Map<String, String> systemProperties) {
        delegate.withSystemProperties(systemProperties);
        return this;
    }

    @Override
    public CamelCliLauncher withEnv(String name, String value) {
        delegate.withEnv(name, value);
        return this;
    }

    @Override
    public CamelCliLauncher withEnvs(Map<String, String> envVars) {
        delegate.withEnvs(envVars);
        return this;
    }

    @Override
    public CamelCliLauncher workingDir(Path workingDir) {
        delegate.workingDir(workingDir);
        return this;
    }

    @Override
    public Path getWorkingDir() {
        return delegate.getWorkingDir();
    }
}
