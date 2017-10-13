package com.consol.citrus.http.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpMessageContentBuilderTest {

    @Test
    public void testHeaderVariableSubstitution() {
        TestContext ctx = new TestContext();
        ctx.setVariable("testHeader", "foo");
        ctx.setVariable("testValue", "bar");

        HttpMessage msg = new HttpMessage("testPayload");
        msg.setHeader("${testHeader}", "${testValue}");

        HttpMessageContentBuilder builder = new HttpMessageContentBuilder(msg, new StaticMessageContentBuilder(msg));

        Message builtMessage = builder.buildMessageContent(ctx, String.valueOf(MessageType.XML));

        Assert.assertEquals(builtMessage.getHeaders().entrySet().size(), 3);
        Assert.assertEquals(builtMessage.getHeader("foo"), "bar");
    }
}
