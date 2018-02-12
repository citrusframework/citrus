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

package com.consol.citrus.generate.javadsl;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.generate.AbstractTestGenerator;
import com.consol.citrus.generate.UnitFramework;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JavaTestGenerator<T extends JavaTestGenerator> extends AbstractTestGenerator<T> {

    /** Actor describing which part (client/server) to use */
    private GeneratorMode mode = GeneratorMode.CLIENT;

    public JavaTestGenerator() {
        withFileExtension(".java");
    }

    @Override
    public void create() {
        if (Character.isLowerCase(getName().charAt(0))) {
            throw new CitrusRuntimeException("Test name must start with an uppercase letter");
        }

        createJavaTest();
    }

    /**
     * Create the Java test with type and method information.
     */
    protected void createJavaTest() {
        TypeSpec.Builder testTypeBuilder = TypeSpec.classBuilder(getName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(getJavaDoc())
                .addMethod(getTestMethod(getMethodName()));

        if (getFramework().equals(UnitFramework.JUNIT5)) {
            testTypeBuilder.addAnnotation(getBaseExtension());
        } else {
            testTypeBuilder.superclass(getBaseType());
        }

        JavaFile javaFile = JavaFile.builder(getTargetPackage(), testTypeBuilder.build())
                .indent("    ")
                .build();

        try {
            javaFile.writeTo(new File(getSrcDirectory()));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write java class file", e);
        }
    }

    /**
     * Gets the class java doc.
     * @return
     */
    protected CodeBlock getJavaDoc() {
        return CodeBlock.builder()
                .add("$L\n\n", Optional.ofNullable(getDescription()).orElse(getName()))
                .add("@author $L\n", getAuthor())
                .add("@since $L\n", getCreationDate())
                .build();
    }

    /**
     * Gets the test class base type to extend from.
     * @return
     */
    protected TypeName getBaseType() {
        if (getFramework().equals(UnitFramework.TESTNG)) {
            return ClassName.get("com.consol.citrus.testng", "AbstractTestNGCitrusTest");
        } else if (getFramework().equals(UnitFramework.JUNIT4)) {
            return ClassName.get("com.consol.citrus.junit", "AbstractJUnit4CitrusTest");

        }

        throw new CitrusRuntimeException("Unsupported framework: " + getFramework());
    }

    /**
     * Gets the Junit5 base extension to use.
     * @return
     */
    protected AnnotationSpec getBaseExtension() {
        return AnnotationSpec.builder(ClassName.get("org.junit.jupiter.api.extension","ExtendWith"))
                .addMember("value", "com.consol.citrus.junit.jupiter.CitrusBaseExtension")
                .build();
    }

    /**
     * Gets the test method spec with test logic.
     * @param name
     * @return
     */
    protected MethodSpec getTestMethod(String name) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getCitrusAnnotation());

        Stream.of(getTestAnnotations()).forEach(methodBuilder::addAnnotation);

        getActions().forEach(action -> methodBuilder.addCode(action)
                                                    .addCode("\n\n"));

        return methodBuilder.build();
    }

    /**
     * Gets the Citrus test annotation to use.
     * @return
     */
    protected AnnotationSpec getCitrusAnnotation() {
        return AnnotationSpec.builder(CitrusXmlTest.class)
                .addMember("name", "$S", getName())
                .build();
    }

    /**
     * Gets the unit framework annotation to use.
     * @return
     */
    protected AnnotationSpec[] getTestAnnotations() {
        if (getFramework().equals(UnitFramework.TESTNG)) {
            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.testng.annotations", "Test"));

            if (isDisabled()) {
                builder.addMember("enabled", "false");
            }

            return new AnnotationSpec[] { builder.build() };
        } else if (getFramework().equals(UnitFramework.JUNIT4)) {
            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.junit", "Test"));

            if (isDisabled()) {
                return new AnnotationSpec[] {builder.build(), AnnotationSpec.builder(ClassName.get("org.junit", "Ignore")).build() };
            }

            return new AnnotationSpec[] { builder.build() };

        } else if (getFramework().equals(UnitFramework.JUNIT5)) {
            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.junit.jupiter.api", "Test"));

            if (isDisabled()) {
                return new AnnotationSpec[] {builder.build(), AnnotationSpec.builder(ClassName.get("org.junit.jupiter.api", "Disabled")).build() };
            }

            return new AnnotationSpec[] { builder.build() };
        }

        throw new CitrusRuntimeException("Unsupported framework: " + getFramework());
    }

    /**
     * List of test actions to be added as code to the method body section of the test.
     * @return
     */
    protected List<CodeBlock> getActions() {
        return Collections.emptyList();
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

    @Override
    public String getSrcDirectory() {
        return super.getSrcDirectory() + File.separator + "java";
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
