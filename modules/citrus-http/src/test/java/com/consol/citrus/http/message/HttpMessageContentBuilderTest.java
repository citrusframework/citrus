/*
 * Copyright 2006-2017 the original author or authors.
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


package com.consol.citrus.http.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpMessageContentBuilderTest {

    @Test
    public void testHeaderVariableSubstitution() {

        //GIVEN
        final TestContext ctx = new TestContext();
        ctx.setVariable("testHeader", "foo");
        ctx.setVariable("testValue", "bar");

        final HttpMessage msg = new HttpMessage("testPayload");
        msg.setHeader("${testHeader}", "${testValue}");

        final HttpMessageContentBuilder builder =
                new HttpMessageContentBuilder(msg, new StaticMessageContentBuilder(msg));

        //WHEN
        final Message builtMessage = builder.buildMessageContent(ctx, String.valueOf(MessageType.XML));

        //THEN
        Assert.assertEquals(builtMessage.getHeaders().entrySet().size(), 4);
        Assert.assertEquals(msg.getHeader(MessageHeaders.ID), builtMessage.getHeader(MessageHeaders.ID));
        Assert.assertEquals(msg.getHeader(MessageHeaders.TIMESTAMP), builtMessage.getHeader(MessageHeaders.TIMESTAMP));
        Assert.assertEquals(MessageType.XML.toString(), builtMessage.getHeader(MessageHeaders.MESSAGE_TYPE));
        Assert.assertEquals(builtMessage.getHeader("foo"), "bar");
    }
}
