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

package org.citrusframework.nosql.spi;

import java.util.Objects;

/**
 * Fully substituted operation handed to a provider. The operation is immutable and contains no
 * unresolved Citrus expressions and no provider SDK objects.
 */
public record NoSqlOperation(NoSqlOperationKind kind,
                             String nativeCommand,
                             String target,
                             NoSqlPayload payload,
                             NoSqlOperationOptions options) {

    public NoSqlOperation {
        Objects.requireNonNull(kind, "Operation kind must not be null");
        Objects.requireNonNull(target, "Operation target must not be null");
        Objects.requireNonNull(payload, "Operation payload must not be null");
        Objects.requireNonNull(options, "Operation options must not be null");
    }

    public NoSqlOperation(NoSqlOperationKind kind,
                          String target,
                          NoSqlPayload payload,
                          NoSqlOperationOptions options) {
        this(kind, null, target, payload, options);
    }

    public static NoSqlOperation of(NoSqlOperationKind kind, String target, NoSqlPayload payload) {
        return new NoSqlOperation(kind, null, target, payload, NoSqlOperationOptions.defaults());
    }

    /**
     * Renders the operation without the payload. The payload may contain sensitive document content
     * and is never rendered.
     */
    @Override
    public String toString() {
        return String.format("NoSqlOperation[kind=%s, nativeCommand=%s, target=%s]",
                kind, nativeCommand, target);
    }
}
