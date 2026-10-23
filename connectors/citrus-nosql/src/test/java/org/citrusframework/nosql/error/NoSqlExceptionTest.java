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

package org.citrusframework.nosql.error;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class NoSqlExceptionTest {

    @Test
    public void shouldCarryErrorClassAndCause() {
        IllegalStateException cause = new IllegalStateException("root cause");

        NoSqlException exception = new NoSqlException(NoSqlErrorClass.CONNECTION, "connection failed", cause);

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONNECTION);
        assertEquals(exception.getMessage(), "connection failed");
        assertSame(exception.getCause(), cause);
        assertFalse(exception.isWriteOutcomeUnknown());
        assertInstanceOf(CitrusRuntimeException.class, exception);
    }

    @Test
    public void shouldFlagUnknownWriteOutcomeOnTimeout() {
        NoSqlException exception = NoSqlException.writeTimeout("write timed out", null);

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.TIMEOUT);
        assertTrue(exception.isWriteOutcomeUnknown());
    }
}
