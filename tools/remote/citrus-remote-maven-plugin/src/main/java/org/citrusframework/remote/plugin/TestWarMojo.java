/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.remote.plugin;

import org.apache.maven.plugins.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo(name = "test-war", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST)
public class TestWarMojo extends TestJarMojo {

    @Parameter(property = "citrus.skip.test.war", defaultValue = "false")
    protected boolean skipTestWar;
    
    @Override
    protected String getDefaultDescriptorRef() {
        return "test-war";
    }

    @Override
    protected boolean shouldSkip() {
        return skipTestWar;
    }
}
