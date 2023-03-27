/*
 * Copyright 2006-2012 the original author or authors.
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
package org.citrusframework.citrus.functions.core;

import java.util.Collections;

import org.citrusframework.citrus.UnitTestSupport;
import org.citrusframework.citrus.exceptions.InvalidFunctionUsageException;
import org.citrusframework.citrus.functions.FunctionParameterHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DigestAuthHeaderFunctionTest extends UnitTestSupport {

    DigestAuthHeaderFunction function = new DigestAuthHeaderFunction();

    @Test
    public void testFunction() {
        String digestHeader = function.execute(FunctionParameterHelper.getParameterList("'username', 'password', 'authRealm', 'acegi', 'POST', 'http://localhost:8080', 'citrus', 'md5'"), context);

        Assert.assertTrue(digestHeader.startsWith("Digest username=username,realm=authRealm,nonce="));
        Assert.assertTrue(digestHeader.contains("uri=http://localhost:8080,response="));
        Assert.assertTrue(digestHeader.contains("algorithm=md5"));
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList(), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testMissingParameters() {
        function.execute(FunctionParameterHelper.getParameterList("'username', 'password', 'authRealm', 'http://localhost:8080', 'citrus', 'md5'"), context);
    }
}
