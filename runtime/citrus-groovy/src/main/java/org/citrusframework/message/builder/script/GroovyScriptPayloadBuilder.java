/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message.builder.script;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.ScriptPayloadBuilder;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.validation.script.TemplateBasedScriptBuilder;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * @author Christoph Deppisch
 */
public class GroovyScriptPayloadBuilder implements ScriptPayloadBuilder {

    /** Default path to script template */
    private final Resource scriptTemplateResource = Resources.fromClasspath("org/citrusframework/script/markup-builder-template.groovy");

    private String script;

    private GroovyFileResourcePayloadBuilder delegate;

    /**
     * Default constructor;
     */
    public GroovyScriptPayloadBuilder() {
    }

    /**
     * Default constructor using payload script.
     * @param script
     */
    public GroovyScriptPayloadBuilder(String script) {
        this.script = script;
    }

    /**
     * Default constructor using payload file resource.
     * @param file
     */
    public GroovyScriptPayloadBuilder(Resource file) {
        this.delegate = new GroovyFileResourcePayloadBuilder(file);
    }

    @Override
    public Object buildPayload(TestContext context) {
        if (delegate != null) {
            return delegate.buildPayload(context);
        }

        return buildMarkupBuilderScript(context.replaceDynamicContentInString(script));
    }

    /**
     * Builds an automatic Groovy MarkupBuilder script with given script body.
     *
     * @param scriptData
     * @return
     */
    protected String buildMarkupBuilderScript(String scriptData) {
        try {
            ClassLoader parent = GroovyScriptPayloadBuilder.class.getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);

            Class<?> groovyClass = loader.parseClass(TemplateBasedScriptBuilder.fromTemplateResource(scriptTemplateResource)
                    .withCode(scriptData)
                    .build());

            if (groovyClass == null) {
                throw new CitrusRuntimeException("Could not load groovy script!");
            }

            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            return (String) groovyObject.invokeMethod("run", new Object[] {});
        } catch (CompilationFailedException | InstantiationException | IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    @Override
    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public void setFile(String file) {
        delegate = new GroovyFileResourcePayloadBuilder(file);
    }

    @Override
    public void setFile(String file, String charset) {
        delegate = new GroovyFileResourcePayloadBuilder(file, charset);
    }
}
