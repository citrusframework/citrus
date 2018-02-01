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

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JavaTestCreator extends AbstractTemplateBasedTestCreator<JavaTestCreator> {

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
    protected String getTemplateFilePath() {
        return "classpath:com/consol/citrus/creator/java-" + getFramework().name().toLowerCase() + "-template.txt";
    }

    @Override
    public String getSrcDirectory() {
        return super.getSrcDirectory() + File.separator + "java";
    }

}
