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

package org.citrusframework.container;

import java.io.File;

import org.citrusframework.condition.FileCondition;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class WaitFileConditionBuilder extends WaitConditionBuilder<FileCondition, WaitFileConditionBuilder> {

    /**
     * Default constructor using fields.
     * @param builder
     */
    public WaitFileConditionBuilder(Wait.Builder<FileCondition> builder) {
        super(builder);
    }

    /**
     * Wait for given file path.
     * @param filePath
     * @return
     */
    public WaitFileConditionBuilder path(String filePath) {
        getCondition().setFilePath(filePath);
        return self;
    }

    /**
     * Wait for given file resource.
     * @param file
     * @return
     */
    public WaitFileConditionBuilder resource(File file) {
        getCondition().setFile(file);
        return self;
    }
}
