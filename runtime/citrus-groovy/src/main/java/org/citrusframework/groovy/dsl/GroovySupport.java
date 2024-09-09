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

package org.citrusframework.groovy.dsl;

import java.io.IOException;
import java.util.Arrays;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Support class combining all available Groovy capabilities.
 */
public class GroovySupport {

    private TestContext context;
    private Object delegate;

    /**
     * Static entrance for all Camel related Java DSL functionalities.
     * @return
     */
    public static GroovySupport groovy() {
        return new GroovySupport();
    }

    /**
     * Loads given Groovy script and return the constructed Java object.
     * @param script
     * @return
     */
    public Object load(String script) {
        return load(script, new ImportCustomizer());
    }

    /**
     * Loads given Groovy script and return the constructed Java object.
     * @param script
     * @return
     */
    public Object load(String script, String... imports) {
        ImportCustomizer importCustomizer = new ImportCustomizer();

        String[] packageNames = Arrays.stream(imports)
                .filter(it -> it.endsWith(".*"))
                .map(it -> it.substring(0, it.length() - 2)) // remove ".*" suffix
                .toArray(String[]::new);

        String[] classNames = Arrays.stream(imports)
                .filter(it -> !it.endsWith(".*"))
                .toArray(String[]::new);

        if (packageNames.length > 0) {
            importCustomizer.addStarImports(packageNames);
        }

        if (classNames.length > 0) {
            importCustomizer.addImports(classNames);
        }

        return load(script, importCustomizer);
    }

    /**
     * Loads given Groovy script and return the constructed Java object.
     * @param script
     * @return
     */
    public Object load(String script, ImportCustomizer importCustomizer) {
        String resolvedScript;
        if (context != null) {
            resolvedScript = context.replaceDynamicContentInString(script);
        } else {
            resolvedScript = script;
        }

        if (delegate != null) {
            return GroovyShellUtils.run(importCustomizer, delegate, resolvedScript, null, context);
        } else {
            return GroovyShellUtils.run(importCustomizer, resolvedScript, null, context);
        }
    }

    /**
     * Loads given file resource content as a Groovy script and return the constructed Java object.
     * @param resource
     * @return
     */
    public Object load(Resource resource) {
        try {
            return load(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read Groovy script resource", e);
        }
    }

    public GroovySupport withDelegate(Object delegate) {
        this.delegate = delegate;
        return this;
    }

    public GroovySupport withTestContext(TestContext context) {
        this.context = context;
        return this;
    }
}
