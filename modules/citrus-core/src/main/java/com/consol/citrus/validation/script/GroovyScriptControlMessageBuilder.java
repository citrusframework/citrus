/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.validation.script;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.GroovyUtils;
import com.consol.citrus.validation.builder.AbstractHeaderAwareControlMessageBuilder;

/**
 * Builds a control message from Groovy code with XML slurper support.
 * 
 * @author Christoph Deppisch
 */
public class GroovyScriptControlMessageBuilder extends AbstractHeaderAwareControlMessageBuilder<String> {

    /** Control message payload defined in external file resource as Groovy MarkupBuilder script */
    private Resource scriptResource;

    /** Inline control message payload as Groovy MarkupBuilder script */
    private String scriptData;
    
    /**
     * Build the control message from script code.
     */
    public String buildMessagePayload(TestContext context) {
        try {    
            //construct control message payload
            String messagePayload = "";
            if (scriptResource != null){
                messagePayload = GroovyUtils.buildMarkupBuilderScript(context.replaceDynamicContentInString(FileUtils.readToString(scriptResource)));
            } else if (scriptData != null){
                messagePayload = GroovyUtils.buildMarkupBuilderScript(context.replaceDynamicContentInString(scriptData));
            }
            
            return messagePayload;
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to build control message payload", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to build control message payload", e);
        }
    }
    
    /**
     * Set message payload data as inline Groovy MarkupBuilder script.
     * @param scriptData the scriptData to set
     */
    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }

    /**
     * Message payload as external Groovy MarkupBuilder script file resource.
     * @param scriptResource the scriptResource to set
     */
    public void setScriptResource(Resource scriptResource) {
        this.scriptResource = scriptResource;
    }
}
