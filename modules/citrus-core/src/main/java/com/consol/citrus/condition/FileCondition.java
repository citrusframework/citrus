/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.condition;

import com.consol.citrus.context.TestContext;

import java.io.File;

/**
 * Tests for the presence of a file and returns true if the file exists
 *
 * @author Martin Maher
 * @since 2.4
 */
public class FileCondition implements Condition {

    private String filename;

    public String getFilename() {
        return filename;
    }

    private File getFile(TestContext context) {
        return new File(context.resolveDynamicValue(filename));
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean isSatisfied(TestContext context) {
        return getFile(context).isFile();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileCondition condition = (FileCondition) o;

        return !(filename != null ? !filename.equals(condition.filename) : condition.filename != null);

    }

    @Override
    public int hashCode() {
        return filename != null ? filename.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FileCondition{" +
                "filename='" + filename + '\'' +
                '}';
    }
}
