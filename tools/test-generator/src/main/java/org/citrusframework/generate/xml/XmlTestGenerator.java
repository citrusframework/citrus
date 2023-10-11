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

package org.citrusframework.generate.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import org.citrusframework.generate.AbstractTemplateBasedTestGenerator;
import org.citrusframework.generate.TestGenerator;
import org.citrusframework.generate.javadsl.JavaTestGenerator;
import org.citrusframework.model.testcase.core.EchoActionType;
import org.citrusframework.model.testcase.core.ObjectFactory;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.xml.StringResult;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class XmlTestGenerator<T extends XmlTestGenerator> extends AbstractTemplateBasedTestGenerator<T> {

    /** Actor describing which part (client/server) to use */
    private GeneratorMode mode = GeneratorMode.CLIENT;

    /** XML fragment marshaller for test actions */
    private volatile TestActionMarshaller marshaller;

    public XmlTestGenerator() {
        withFileExtension(FileUtils.FILE_EXTENSION_XML);
    }

    /**
     * Set the mode describing which part (client/server) to use.
     * @param mode
     * @return
     */
    public T withMode(GeneratorMode mode) {
        this.mode = mode;
        return self;
    }

    /**
     * Marshaller context paths. Subclasses may add additional packages.
     * @return
     */
    protected List<String> getMarshallerContextPaths() {
        List<String> contextPaths = new ArrayList<>();
        contextPaths.add(ObjectFactory.class.getPackage().getName());
        return contextPaths;
    }

    /**
     * Marshaller schema. Subclasses may overwrite schema or set to null to disable schema validation of marshalled data.
     * @return
     */
    protected List<Resource> getMarshallerSchemas() {
        List<Resource> schemas = new ArrayList<>();
        schemas.add(Resources.create("org/citrusframework/schema/citrus-testcase.xsd"));
        return schemas;
    }

    @Override
    public void create() {
        super.create();
        getJavaTestGenerator().create();
    }

    @Override
    protected Properties getTemplateProperties() {
        Properties properties = super.getTemplateProperties();

        properties.put("test.actions", getActions().stream().map(action -> {
            StringResult result = new StringResult();
            createMarshaller().marshal(action, result);
            return Pattern.compile("^", Pattern.MULTILINE).matcher(result.toString()).replaceAll("        ");
        }).collect(Collectors.joining("\n\n")));

        return properties;
    }

    private TestActionMarshaller createMarshaller() {
        if (marshaller == null) {
            synchronized (this) {
                marshaller = new TestActionMarshaller(getMarshallerSchemas().toArray(new Resource[0]), getMarshallerContextPaths().toArray(new String[0]));
            }
        }

        return marshaller;
    }

    /**
     * List of test actions to be marshalled in the actions section of the test.
     * @return
     */
    protected List<Object> getActions() {
        List<Object> actions = new ArrayList<>();
        EchoActionType echo = new EchoActionType();
        echo.setMessage("TODO: Code the test " + getName());
        actions.add(new JAXBElement<>(new QName("http://www.citrusframework.org/schema/testcase", "echo"), EchoActionType.class, echo));
        return actions;
    }

    /**
     * Gets Java test generator for this XML test.
     * @return
     */
    protected TestGenerator getJavaTestGenerator() {
        return new JavaTestGenerator()
                .withName(getName())
                .withDisabled(isDisabled())
                .withDescription(getDescription())
                .withAuthor(getAuthor())
                .withFramework(getFramework())
                .usePackage(getTargetPackage())
                .useSrcDirectory(super.getSrcDirectory());
    }

    @Override
    protected String getTemplateFilePath() {
        return "classpath:org/citrusframework/generate/test-template.xml";
    }

    @Override
    public String getSrcDirectory() {
        return super.getSrcDirectory() + File.separator + "resources";
    }

    /**
     * Sets the marshaller.
     *
     * @param marshaller
     */
    public void setMarshaller(TestActionMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Gets the marshaller.
     *
     * @return
     */
    public TestActionMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Gets the mode.
     *
     * @return
     */
    public GeneratorMode getMode() {
        return mode;
    }

    /**
     * Sets the mode.
     *
     * @param mode
     */
    public void setMode(GeneratorMode mode) {
        this.mode = mode;
    }

}
