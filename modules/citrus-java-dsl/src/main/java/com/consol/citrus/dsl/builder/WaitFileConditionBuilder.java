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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.condition.FileCondition;
import com.consol.citrus.container.Wait;

import java.io.File;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class WaitFileConditionBuilder extends WaitConditionBuilder<FileCondition, WaitFileConditionBuilder> {

    /**
     * Default constructor using fields.
     * @param condition
     * @param builder
     */
    public WaitFileConditionBuilder(FileCondition condition, WaitBuilder builder) {
        super(condition, builder);
    }

    /**
     * Wait for given file path.
     * @param filePath
     * @return
     */
    public Wait path(String filePath) {
        getCondition().setFilePath(filePath);
        return getBuilder().buildAndRun();
    }

    /**
     * Wait for given file resource.
     * @param file
     * @return
     */
    public Wait resource(File file) {
        getCondition().setFile(file);
        return getBuilder().buildAndRun();
    }
}
