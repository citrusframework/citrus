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

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.dsl.runner.TestRunner;
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
    private void createJavaTest() {
        final TypeSpec.Builder testTypeBuilder = TypeSpec.classBuilder(getName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(getJavaDoc())
                .addMethod(getTestMethod(getMethodName()));

        if (getFramework().equals(UnitFramework.JUNIT5)) {
            testTypeBuilder.addAnnotation(getBaseExtension());
        } else {
            testTypeBuilder.superclass(getBaseType());
        }

        final JavaFile javaFile = JavaFile.builder(getTargetPackage(), testTypeBuilder.build())
                .indent("    ")
                .build();

        try {
            javaFile.writeTo(new File(getSrcDirectory()));
        } catch (final IOException e) {
            throw new CitrusRuntimeException("Failed to write java class file", e);
        }
    }

    /**
     * Gets the class java doc.
     * @return The javadoc CodeBlock
     */
    private CodeBlock getJavaDoc() {
        return CodeBlock.builder()
                .add("$L\n\n", Optional.ofNullable(getDescription()).orElse(getName()))
                .add("@author $L\n", getAuthor())
                .add("@since $L\n", getCreationDate())
                .build();
    }

    /**
     * Gets the test class base type to extend from.
     * @return TypeName of the base type
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
     * @return The AnnotationSpec of the Junit5 extension
     */
    protected AnnotationSpec getBaseExtension() {
        return createAnnotationBuilder("org.junit.jupiter.api.extension", "ExtendWith")
                .addMember("value", "com.consol.citrus.junit.jupiter.CitrusBaseExtension")
                .build();
    }

    /**
     * Gets the test method spec with test logic.
     * @param name The name of the test
     * @return The method specification
     */
    private MethodSpec getTestMethod(final String name) {
        final ParameterSpec.Builder methodParamBuilder = ParameterSpec
                .builder(TestRunner.class, "testRunner")
                .addAnnotation(CitrusResource.class);

        if(getFramework().equals(UnitFramework.TESTNG)){
            methodParamBuilder.addAnnotation(createTestNgAnnotationBuilder("Optional").build());
        }

        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getCitrusAnnotation())
                .addParameter(methodParamBuilder.build());

        Stream.of(getTestAnnotations()).forEach(methodBuilder::addAnnotation);

        getActions().forEach(action -> methodBuilder.addCode(action)
                                                    .addCode("\n\n"));

        return methodBuilder.build();
    }

    /**
     * Gets the Citrus XML test annotation.
     * @return The AnnotationSpec for XML tests
     */
    protected AnnotationSpec getCitrusAnnotation() {
        return AnnotationSpec.builder(CitrusXmlTest.class)
                .addMember("name", "$S", getName())
                .build();
    }

    /**
     * Gets the unit framework annotation to use.
     * @return The annotation spec for test cases
     */
    private AnnotationSpec[] getTestAnnotations() {
        switch (getFramework()){
            case JUNIT4: return createJunit4TestAnnotations();
            case JUNIT5: return createJunit5Annotations();
            case TESTNG: return createTestNgTestAnnotations();
            default: throw new CitrusRuntimeException("Unsupported framework: " + getFramework());
        }
    }

    /**
     * List of test actions to be added as code to the method body section of the test.
     * @return A list of actions to execute
     */
    protected List<CodeBlock> getActions() {
        return Collections.emptyList();
    }

    /**
     * Set the mode describing which part (client/server) to use.
     * @param mode The mode to generate the test for
     * @return The modified JavaTestGenerator
     */
    public T withMode(final GeneratorMode mode) {
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
     * @return The current mode
     */
    public GeneratorMode getMode() {
        return mode;
    }

    /**
     * Sets the mode.
     *
     * @param mode The mode to set (client/server
     */
    public void setMode(final GeneratorMode mode) {
        this.mode = mode;
    }

    private AnnotationSpec[] createJunit5Annotations() {
        return createHJunitTestAnnotations(
                createAnnotationBuilder("org.junit.jupiter.api", "Test"),
                createAnnotationBuilder("org.junit.jupiter.api", "Disabled"));
    }

    private AnnotationSpec[] createJunit4TestAnnotations() {
        return createHJunitTestAnnotations(
                createAnnotationBuilder("org.junit", "Test"),
                createAnnotationBuilder("org.junit", "Ignore"));
    }

    private AnnotationSpec[] createTestNgTestAnnotations() {
        final AnnotationSpec.Builder testAnnotationBuilder = createTestNgAnnotationBuilder("Test");

        if (isDisabled()) {
            testAnnotationBuilder.addMember("enabled", "false");
        }

        final AnnotationSpec.Builder parametersBuilder = createTestNgAnnotationBuilder("Parameters");
        parametersBuilder.addMember("value","$S", "testRunner");

        return new AnnotationSpec[] { testAnnotationBuilder.build(), parametersBuilder.build() };
    }

    private AnnotationSpec[] createHJunitTestAnnotations(final AnnotationSpec.Builder testAnnotation,
                                                         final AnnotationSpec.Builder disabledAnnotation){
        if (isDisabled()) {
            return new AnnotationSpec[] {testAnnotation.build(), disabledAnnotation.build()};
        }
        return new AnnotationSpec[] {testAnnotation.build()};
    }

    private ClassName getTestNgAnnotation(final String annotationName) {
        return ClassName.get("org.testng.annotations", annotationName);
    }

    private AnnotationSpec.Builder createTestNgAnnotationBuilder(final String parameters) {
        return AnnotationSpec.builder(getTestNgAnnotation(parameters));
    }

    private AnnotationSpec.Builder createAnnotationBuilder(final String packageName, final String simpleName) {
        return AnnotationSpec.builder(ClassName.get(packageName, simpleName));
    }
}
