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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.actions.EchoAction;
import org.citrusframework.annotations.CitrusTest;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JavaDslTestGenerator<T extends JavaDslTestGenerator<T>> extends JavaTestGenerator<T> {

    @Override
    protected AnnotationSpec getCitrusAnnotation() {
        return AnnotationSpec.builder(CitrusTest.class).build();
    }

    @Override
    protected JavaFile.Builder createJavaFileBuilder(TypeSpec.Builder testTypeBuilder) {
        return super.createJavaFileBuilder(testTypeBuilder)
                .addStaticImport(EchoAction.Builder.class, "echo");
    }

    @Override
    protected AnnotationSpec getBaseExtension() {
        ClassName extension = ClassName.get("org.citrusframework.junit.jupiter", "CitrusExtension");
        return createAnnotationBuilder("org.junit.jupiter.api.extension","ExtendWith")
                .addMember("value", "$T.class", extension)
                .build();
    }

    @Override
    protected List<CodeBlock> getActions() {
        List<CodeBlock> codeBlocks = new ArrayList<>();
        codeBlocks.add(CodeBlock.builder().add("runner.run(echo(\"TODO: Code the test $L\"));", getName()).build());
        return codeBlocks;
    }
}
