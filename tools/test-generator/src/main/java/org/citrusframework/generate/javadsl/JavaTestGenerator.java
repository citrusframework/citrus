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

package org.citrusframework.generate.javadsl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.AbstractTestGenerator;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.util.FileUtils;

/**
 * @since 2.7.4
 */
public class JavaTestGenerator<T extends JavaTestGenerator<T>> extends AbstractTestGenerator<T> {

    /** Actor describing which part (client/server) to use */
    private GeneratorMode mode = GeneratorMode.CLIENT;

    public JavaTestGenerator() {
        withFileExtension(FileUtils.FILE_EXTENSION_JAVA);
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

        final JavaFile javaFile = createJavaFileBuilder(testTypeBuilder).build();

        try {
            javaFile.writeTo(new File(getSrcDirectory()));
        } catch (final IOException e) {
            throw new CitrusRuntimeException("Failed to write java class file", e);
        }
    }

    protected JavaFile.Builder createJavaFileBuilder(TypeSpec.Builder testTypeBuilder) {
        return JavaFile.builder(getTargetPackage(), testTypeBuilder.build())
                .indent("    ");
    }

    /**
     * Gets the class java doc.
     * @return The javadoc CodeBlock
     */
    private CodeBlock getJavaDoc() {
        return CodeBlock.builder()
                .add("$L\n\n", Optional.ofNullable(getDescription()).orElseGet(this::getName))
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
            return ClassName.get("org.citrusframework.testng", "TestNGCitrusSupport");
        } else if (getFramework().equals(UnitFramework.JUNIT4)) {
            return ClassName.get("org.citrusframework.junit", "JUnit4CitrusSupport");
        }

        throw new CitrusRuntimeException("Unsupported framework: " + getFramework());
    }

    /**
     * Gets the Junit5 base extension to use.
     * @return The AnnotationSpec of the Junit5 extension
     */
    protected AnnotationSpec getBaseExtension() {
        ClassName extension = ClassName.get("org.citrusframework.junit.jupiter", "CitrusBaseExtension");
        return createAnnotationBuilder("org.junit.jupiter.api.extension", "ExtendWith")
                .addMember("value", "$T.class", extension)
                .build();
    }

    /**
     * Gets the test method spec with test logic.
     * @param name The name of the test
     * @return The method specification
     */
    private MethodSpec getTestMethod(final String name) {
        final ParameterSpec.Builder methodParamBuilder = ParameterSpec
                .builder(TestCaseRunner.class, "runner")
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
        return AnnotationSpec.builder(CitrusTestSource.class)
                .addMember("type", "$S", "xml")
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
        parametersBuilder.addMember("value","$S", "runner");

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

    protected AnnotationSpec.Builder createAnnotationBuilder(final String packageName, final String simpleName) {
        return AnnotationSpec.builder(ClassName.get(packageName, simpleName));
    }
}
