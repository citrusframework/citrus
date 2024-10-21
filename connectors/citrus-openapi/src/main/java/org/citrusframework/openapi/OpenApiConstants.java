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

package org.citrusframework.openapi;

public final class OpenApiConstants {

    public static final String TYPE_ARRAY = "array";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_OBJECT = "object";
    public static final String TYPE_STRING = "string";

    public static final String FORMAT_INT32 = "int32";
    public static final String FORMAT_INT64 = "int64";
    public static final String FORMAT_FLOAT = "float";
    public static final String FORMAT_DOUBLE = "double";
    public static final String FORMAT_DATE = "date";
    public static final String FORMAT_DATE_TIME = "date-time";
    public static final String FORMAT_UUID = "uuid";

    /**
     * Prevent instantiation.
     */
    private OpenApiConstants() {
    }
}
