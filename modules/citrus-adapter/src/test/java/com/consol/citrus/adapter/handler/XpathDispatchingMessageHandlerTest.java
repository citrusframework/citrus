package com.consol.citrus.adapter.handler;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit Tests for XpathDispatchingMessageHandler
 * @author jza
 */
public class XpathDispatchingMessageHandlerTest {

    /**
     * Test for handler routing by node content
     */
    @Test
    public void testRouteMessageByElementTextContent() {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/XpathDispatchingMessageHandlerTest-context.xml");
        handler.setXpathMappingExpression("//MessageBody/*");

        Message<?> response = handler.handleMessage(
                MessageBuilder.withPayload("<MessageBody><EmptyResponseRequest>emptyResponse please " +
                        "</EmptyResponseRequest></MessageBody>").build());

        Assert.assertFalse(StringUtils.hasText((String) response.getPayload()));

        response = handler.handleMessage(
                MessageBuilder.withPayload("<MessageBody><ContentResponseRequest>contentResponse please " +
                        "</ContentResponseRequest></MessageBody>").build());

        Assert.assertEquals(response.getPayload(), "Here is your payload, dude");
    }

    /**
     * Test for handler routing without Xpath given (implementation takes the value of first node).
     */
    @Test
    public void testRouteMessageWithoutXpath() {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/XpathDispatchingMessageHandlerTest-context.xml");

        Message<?> response = handler.handleMessage(
                MessageBuilder.withPayload(
                    "<EmptyResponseRequest>emptyResponse please</EmptyResponseRequest>").build());

        Assert.assertFalse(StringUtils.hasText((String) response.getPayload()));

        response = handler.handleMessage(
                MessageBuilder.withPayload(
                    "<ContentResponseRequest>contentResponse please</ContentResponseRequest>").build());

        Assert.assertEquals(response.getPayload(), "Here is your payload, dude");
    }

    /**
     * Test for Xpath which is not found --> shall raise exception
     */
    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRouteMessageWithBadXpathExpression() {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/XpathDispatchingMessageHandlerTest-context.xml");
        handler.setXpathMappingExpression("//I_DO_NOT_EXIST");

        handler.handleMessage(MessageBuilder.withPayload(
                    "<MessageBody>emptyResponse</MessageBody>").build());
    }

    /**
     * Test for correct xpath, but no handler bean is found --> shall raise exc
     */
    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRouteMessageWithBadHandlerConfiguration() {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/XpathDispatchingMessageHandlerTest-context.xml");
        handler.setXpathMappingExpression("//MessageBody/*");

        handler.handleMessage(MessageBuilder.withPayload(
                    "<MessageBody><NoSuchBean>This bean does not exist</NoSuchBean></MessageBody>").build());
    }
}
