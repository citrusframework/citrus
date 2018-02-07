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

package com.consol.citrus.creator;

import com.consol.citrus.exceptions.CitrusRuntimeException;

import java.io.File;
import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JavaTestCreator extends AbstractTemplateBasedTestCreator<JavaTestCreator> {

    protected static final String TEST_BASE_CLASS_IMPORT = "test.base.class.import";
    protected static final String TEST_UNIT_FRAMEWORK_IMPORT = "test.unit.framework.import";
    protected static final String TEST_BASE_CLASS = "test.base.class";

    public JavaTestCreator() {
        withFileExtension(".java");
    }

    @Override
    public void create() {
        if (Character.isLowerCase(getName().charAt(0))) {
            throw new CitrusRuntimeException("Test name must start with an uppercase letter");
        }

        super.create();
    }

    @Override
    protected Properties getTemplateProperties() {
        Properties properties = super.getTemplateProperties();

        if (getFramework().equals(UnitFramework.TESTNG)) {
            properties.put(TEST_UNIT_FRAMEWORK_IMPORT, "org.testng.annotations.Test");
            properties.put(TEST_BASE_CLASS_IMPORT, "com.consol.citrus.testng.AbstractTestNGCitrusTest");
            properties.put(TEST_BASE_CLASS, "AbstractTestNGCitrusTest");

        } else if (getFramework().equals(UnitFramework.JUNIT4)) {
            properties.put(TEST_UNIT_FRAMEWORK_IMPORT, "org.junit.Test");
            properties.put(TEST_BASE_CLASS_IMPORT, "com.consol.citrus.junit.AbstractJUnit4CitrusTest");
            properties.put(TEST_BASE_CLASS, "AbstractJUnit4CitrusTest");

        } else if (getFramework().equals(UnitFramework.JUNIT5)) {
            properties.put(TEST_UNIT_FRAMEWORK_IMPORT, "org.junit.jupiter.api.Test");
            properties.put(TEST_BASE_CLASS_IMPORT, "com.consol.citrus.junit.jupiter.CitrusBaseExtension");
            properties.put(TEST_BASE_CLASS, "CitrusBaseExtension");
        }

        return properties;
    }

    @Override
    protected String getTemplateFilePath() {
        if (getFramework().equals(UnitFramework.JUNIT5)) {
            return "classpath:com/consol/citrus/creator/java-xml-junit5-test-template.txt";
        } else {
            return "classpath:com/consol/citrus/creator/java-xml-test-template.txt";
        }
    }

    @Override
    public String getSrcDirectory() {
        return super.getSrcDirectory() + File.separator + "java";
    }
}
