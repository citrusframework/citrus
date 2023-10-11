/*
 * Copyright 2006-2016 the original author or authors.
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

import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class LoadMessageFunctionTest extends UnitTestSupport {

    private final LoadMessageFunction function = new LoadMessageFunction();

    private final Message message = new DefaultMessage("This is a sample message")
            .setHeader("operation", "sampleOperation");

    @Test
    public void testLoadMessagePayload() throws Exception {
        context.getMessageStore().storeMessage("request", message);
        Assert.assertEquals(function.execute(Collections.singletonList("request"), context), "This is a sample message");
        Assert.assertEquals(function.execute(Collections.singletonList("request.body()"), context), "This is a sample message");
    }

    @Test
    public void testLoadMessageHeader() throws Exception {
        context.getMessageStore().storeMessage("request", message);
        Assert.assertEquals(function.execute(Collections.singletonList("request.header(operation)"), context), "sampleOperation");
        Assert.assertEquals(function.execute(Collections.singletonList("request.header('operation')"), context), "sampleOperation");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing header name.*")
    public void testLoadMessageHeaderEmpty() throws Exception {
        context.getMessageStore().storeMessage("request", message);
        function.execute(Collections.singletonList("request.header()"), context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find header 'wrong'.*")
    public void testLoadMessageHeaderUnknown() throws Exception {
        context.getMessageStore().storeMessage("request", message);
        function.execute(Collections.singletonList("request.header('wrong')"), context);
    }
}
