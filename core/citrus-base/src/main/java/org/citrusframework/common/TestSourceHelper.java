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

package org.citrusframework.common;

import java.util.Optional;

import org.citrusframework.TestSource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

public final class TestSourceHelper {

    private TestSourceHelper() {
        // prevent instantiation of utility class
    }

    public static TestSource create(String filePath) {
        if (filePath == null) {
            return null;
        }

        String fileName = FileUtils.getBaseName(FileUtils.getFileName(filePath));
        String ext = FileUtils.getFileExtension(filePath);
        if (StringUtils.hasText(ext)) {
            return new TestSource(ext, fileName, filePath);
        } else {
            return new TestSource("directory", fileName, filePath);
        }
    }

    public static TestSource create(String filePath, String sourceCode) {
        return Optional.ofNullable(create(filePath))
                .map(ts -> ts.sourceCode(sourceCode))
                .orElse(null);
    }
}
