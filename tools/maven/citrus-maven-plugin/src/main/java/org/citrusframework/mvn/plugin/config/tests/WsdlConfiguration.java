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

package org.citrusframework.mvn.plugin.config.tests;

import org.citrusframework.generate.TestGenerator;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class WsdlConfiguration implements Serializable {

    /**
     * Actor (client/server) describing which part to generate test for.
     */
    @Parameter(property = "citrus.wsdl.actor", defaultValue = "CLIENT")
    private String mode = TestGenerator.GeneratorMode.CLIENT.name();

    /**
     * The path to the wsdl from which the suite is generated.
     */
    @Parameter(property = "citrus.wsdl.file")
    private String file;

    /**
     * The operation name to generate tests from.
     */
    @Parameter(property = "citrus.wsdl.operation")
    private String operation;

    /**
     * Optional mapping expressions for generated message content manipulation.
     */
    @Parameter
    private MappingsConfiguration mappings;

    /**
     * Gets the mode.
     *
     * @return
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the mode.
     *
     * @param mode
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Gets the file.
     *
     * @return
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Gets the operation.
     *
     * @return
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation.
     *
     * @param operation
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the mappings.
     *
     * @return
     */
    public MappingsConfiguration getMappings() {
        return mappings;
    }

    /**
     * Sets the mappings.
     *
     * @param mappings
     */
    public void setMappings(MappingsConfiguration mappings) {
        this.mappings = mappings;
    }
}
