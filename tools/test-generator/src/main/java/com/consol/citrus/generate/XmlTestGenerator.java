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

package com.consol.citrus.generate;

import com.consol.citrus.model.testcase.core.EchoModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringResult;

import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class XmlTestGenerator extends AbstractTemplateBasedTestGenerator<XmlTestGenerator> {

    private Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

    public XmlTestGenerator() {
        withFileExtension(".xml");
        marshaller.setSchema(new ClassPathResource("com/consol/citrus/schema/citrus-testcase.xsd"));
        marshaller.setContextPath("com.consol.citrus.model.testcase.core");

        Map<String, Object> marshallerProperties = new HashMap<>();
        marshallerProperties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerProperties.put(Marshaller.JAXB_ENCODING, "UTF-8");
        marshallerProperties.put(Marshaller.JAXB_FRAGMENT, true);
        marshaller.setMarshallerProperties(marshallerProperties);
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
            marshaller.marshal(action, result);
            return Pattern.compile("^", Pattern.MULTILINE).matcher(result.toString()).replaceAll("        ");
        }).collect(Collectors.joining("\n\n")));

        return properties;
    }

    /**
     * List of test actions to be marshalled in the actions section of the test.
     * @return
     */
    protected List<Object> getActions() {
        List<Object> actions = new ArrayList<>();
        EchoModel echo = new EchoModel();
        echo.setMessage("TODO: Code the test " + getName());
        actions.add(echo);
        return actions;
    }

    /**
     * Gets Java test generator for this XML test.
     * @return
     */
    protected TestGenerator getJavaTestGenerator() {
        return new JavaTestGenerator()
                .withName(getName())
                .withDescription(getDescription())
                .withAuthor(getAuthor())
                .withFramework(getFramework())
                .usePackage(getTargetPackage())
                .useSrcDirectory(super.getSrcDirectory());
    }

    @Override
    protected String getTemplateFilePath() {
        return "classpath:com/consol/citrus/generate/test-template.xml";
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
    public void setMarshaller(Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Gets the marshaller.
     *
     * @return
     */
    public Jaxb2Marshaller getMarshaller() {
        return marshaller;
    }
}
