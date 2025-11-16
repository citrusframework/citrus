/*
 * Copyright the original author or authors.
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

package org.citrusframework.functions.core;

import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.util.FileUtils;
import org.citrusframework.yaml.SchemaProperty;

import static java.lang.Boolean.parseBoolean;

/**
 * Function reads file from given file path and returns the complete file content as function result.
 * File content is automatically parsed for test variables.
 * <p>
 * File path can also have test variables as part of the file name or path.
 * <p>
 * The function accepts the following parameters:
 *
 * <ol>
 *     <li>File path of the file resource to read</li>
 *     <li>Boolean value to indicate that the returned value should be base64 encoded. Defaults to false.</li>
 *     <li>Boolean value to indicate that a dynamic replacement should be performed before the content is base64 encoded. Defaults to false.</li>
 * </ol>
 *
 * @since 2.4
 */
public class ReadFileResourceFunction implements ParameterizedFunction<ReadFileResourceFunction.Parameters> {

    @Override
    public String execute(Parameters param, TestContext context) {
        try {
            if (param.isBase64()) {
                if (param.isUseCharset()) {
                    return Base64.encodeBase64String(readFileContent(param.getFilePath(), context, true).getBytes(FileUtils.getCharset(param.getFilePath())));
                } else {
                    return Base64.encodeBase64String(FileUtils.copyToByteArray(FileUtils.getFileResource(param.getFilePath(), context)));
                }
            } else {
                return readFileContent(param.getFilePath(), context, true);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read file", e);
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    /**
     * Read the file content replacing dynamic content in the file content
     */
    private String readFileContent(String filePath, TestContext context, boolean replace) throws IOException {
        String content = FileUtils.readToString(FileUtils.getFileResource(filePath, context), FileUtils.getCharset(filePath));
        return replace ? context.replaceDynamicContentInString(content) : content;
    }

    public static class Parameters implements FunctionParameters {

        private String filePath;
        private boolean base64;
        private boolean useCharset;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Missing file path function parameter");
            }

            setFilePath(parameterList.get(0));

            if (parameterList.size() > 1) {
                setBase64(parseBoolean(parameterList.get(1)));
            }

            if (parameterList.size() > 2) {
                setUseCharset(parseBoolean(parameterList.get(2)));
            }
        }

        public String getFilePath() {
            return filePath;
        }

        @SchemaProperty(required = true, description = "The file path to read.")
        public void setFilePath(String path) {
            this.filePath = path;
        }

        public boolean isBase64() {
            return base64;
        }

        @SchemaProperty(description = "When enabled the read file content is converted into bas64.")
        public void setBase64(boolean paddingOn) {
            this.base64 = paddingOn;
        }

        public boolean isUseCharset() {
            return useCharset;
        }

        @SchemaProperty(advanced = true, description = "When enabled the charset to use is derived from the file path before converting into bas64.")
        public void setUseCharset(boolean useCharset) {
            this.useCharset = useCharset;
        }
    }
}
