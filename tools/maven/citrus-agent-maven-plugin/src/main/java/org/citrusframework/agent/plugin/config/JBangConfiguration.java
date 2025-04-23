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

package org.citrusframework.agent.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;
import org.citrusframework.jbang.CitrusJBang;
import org.citrusframework.jbang.JBangSettings;

public class JBangConfiguration {

    @Parameter(property = "citrus.agent.jbang.enabled", defaultValue = "true")
    private boolean enabled = true;
    @Parameter(property = "citrus.agent.jbang.app", defaultValue = "citrus@citrusframework/citrus")
    private String app;
    @Parameter(property = "citrus.agent.jbang.dump.output")
    private boolean dumpOutput;

    private CitrusJBang citrus;

    public JBangConfiguration() {
        this.app = JBangSettings.getApp();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public boolean isDumpOutput() {
        return dumpOutput;
    }

    public void setDumpOutput(boolean dumpOutput) {
        this.dumpOutput = dumpOutput;
    }

    public CitrusJBang getCitrusJBang() {
        if (citrus == null) {
            citrus = new CitrusJBang(app);
        }

        return citrus;
    }
}
