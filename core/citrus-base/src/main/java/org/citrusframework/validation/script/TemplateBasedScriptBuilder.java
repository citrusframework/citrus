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

package org.citrusframework.validation.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

/**
 * Script builder builds a script with custom code body. Script header and tail come from static
 * script template.
 *
 * @author Christoph Deppisch
 */
public final class TemplateBasedScriptBuilder {

    /** Placeholder identifier for script body in template */
    private static final String BODY_PLACEHOLDER = "@SCRIPTBODY@";

    /** Head and tail for script */
    private final String scriptHead;
    private final String scriptTail;

    /** Code snippet which is dynamically added to the script */
    private String scriptCode = "";

    /**
     * Constructor using script template string.
     * @param scriptTemplate
     */
    private TemplateBasedScriptBuilder(String scriptTemplate) {
        if (!scriptTemplate.contains(BODY_PLACEHOLDER)) {
            throw new CitrusRuntimeException("Invalid script template - please define '" +
                    BODY_PLACEHOLDER + "' placeholder where your code comes in");
        }

        scriptHead = scriptTemplate.substring(0, scriptTemplate.indexOf(BODY_PLACEHOLDER));
        scriptTail = scriptTemplate.substring((scriptTemplate.indexOf(BODY_PLACEHOLDER) + BODY_PLACEHOLDER.length()));
    }

    /**
     * Builds the final script.
     */
    public String build() {
        StringBuilder scriptBuilder = new StringBuilder();
        StringBuilder scriptBody = new StringBuilder();
        String importStmt = "import ";

        try (BufferedReader reader = new BufferedReader(new StringReader(scriptCode))) {
            if (scriptCode.contains(importStmt)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith(importStmt)) {
                        scriptBuilder.append(line);
                        scriptBuilder.append("\n");
                    } else {
                        scriptBody.append((scriptBody.length() == 0 ? "" : "\n"));
                        scriptBody.append(line);
                    }
                }
            } else {
                scriptBody.append(scriptCode);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to construct script from template", e);
        }

        scriptBuilder.append(scriptHead);
        scriptBuilder.append(scriptBody.toString());
        scriptBuilder.append(scriptTail);

        return scriptBuilder.toString();
    }

    /**
     * Adds custom code snippet to this builder.
     *
     * @param code the custom code body
     * @return
     */
    public TemplateBasedScriptBuilder withCode(String code) {
        this.scriptCode = code;
        return this;
    }

    /**
     * Static construction method returning a fully qualified instance of this builder.
     * @param scriptTemplate the script template code.
     * @return instance of this builder.
     */
    public static TemplateBasedScriptBuilder fromTemplateScript(String scriptTemplate) {
        return new TemplateBasedScriptBuilder(scriptTemplate);
    }

    /**
     * Static construction method returning a fully qualified instance of this builder.
     * @param scriptTemplateResource external file resource holding script template code.
     * @return instance of this builder.
     */
    public static TemplateBasedScriptBuilder fromTemplateResource(Resource scriptTemplateResource) {
        try {
            return new TemplateBasedScriptBuilder(FileUtils.readToString(scriptTemplateResource.getInputStream()));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error loading script template from file resource", e);
        }
    }
}
