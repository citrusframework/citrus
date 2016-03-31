/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.functions.core;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;
import com.consol.citrus.util.FileUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.util.List;

/**
 * Function reads file from given file path and returns the complete file content as function result.
 * File content is automatically parsed for test variables.
 *
 * File path can also have test variables as part of the file name or path.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ReadFileResourceFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (CollectionUtils.isEmpty(parameterList)) {
            throw new InvalidFunctionUsageException("Missing file path function parameter");
        }

        boolean base64 = parameterList.size() > 1 ? Boolean.valueOf(parameterList.get(1)) : false;

        try {
            if (base64) {
                return Base64.encodeBase64String(FileCopyUtils.copyToByteArray(FileUtils.getFileResource(parameterList.get(0), context).getInputStream()));
            } else {
                return context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(parameterList.get(0), context)));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read file", e);
        }
    }
}
