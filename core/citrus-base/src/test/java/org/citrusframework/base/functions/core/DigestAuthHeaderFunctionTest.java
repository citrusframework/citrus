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
package org.citrusframework.base.functions.core;

import java.util.Collections;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.FunctionParameterHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DigestAuthHeaderFunctionTest extends UnitTestSupport {

    DigestAuthHeaderFunction function = new DigestAuthHeaderFunction();

    @Test
    public void testFunction() {
        String digestHeader = function.execute(FunctionParameterHelper.getParameterList("'username', 'password', 'authRealm', 'acegi', 'POST', 'http://localhost:8080', 'citrus', 'md5'"), context);

        Assertions.assertTrue(digestHeader.startsWith("Digest username=username,realm=authRealm,nonce="));
        Assertions.assertTrue(digestHeader.contains("uri=http://localhost:8080,response="));
        Assertions.assertTrue(digestHeader.contains("algorithm=md5"));
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> {
            function.execute(Collections.<String>emptyList(), context);
        });
    }

    @Test
    public void testMissingParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> {
            function.execute(FunctionParameterHelper.getParameterList("'username', 'password', 'authRealm', 'http://localhost:8080', 'citrus', 'md5'"), context);
        });
    }
}
