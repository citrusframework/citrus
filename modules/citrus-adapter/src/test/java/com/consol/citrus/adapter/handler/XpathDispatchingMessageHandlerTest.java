package com.consol.citrus.adapter.handler;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit Tests for XpathDispatchingMessageHandler
 * @author jza
 * @deprecated since Citrus 1.4
 */
@Deprecated
public class XpathDispatchingMessageHandlerTest {

    /**
     * Test for handler routing by node content
     */
    @Test
    public void testRouteMessageByElementTextContent() throws Exception {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/test-context.xml");
        handler.setXpathMappingExpression("local-name(//MessageBody/*)");

        handler.afterPropertiesSet();

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
    public void testRouteMessageWithoutXpath() throws Exception {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/test-context.xml");

        handler.afterPropertiesSet();

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
    @Test
    public void testRouteMessageWithBadXpathExpression() throws Exception {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/test-context.xml");
        handler.setXpathMappingExpression("//I_DO_NOT_EXIST");

        handler.afterPropertiesSet();

        try {
            handler.handleMessage(MessageBuilder.withPayload(
                    "<MessageBody>emptyResponse</MessageBody>").build());
            Assert.fail("Missing exception due to bad XPath expression");
        } catch (CitrusRuntimeException e) {
             Assert.assertEquals(e.getMessage(), "No result for XPath expression: '//I_DO_NOT_EXIST'");
        }
    }

    /**
     * Test for correct xpath, but no handler bean is found --> shall raise exc
     */
    @Test
    public void testRouteMessageWithBadHandlerConfiguration() throws Exception {
        XpathDispatchingMessageHandler handler = new XpathDispatchingMessageHandler();
        handler.setMessageHandlerContext(
                "com/consol/citrus/adapter/handler/test-context.xml");
        handler.setXpathMappingExpression("local-name(//MessageBody/*)");

        handler.afterPropertiesSet();

        try {
            handler.handleMessage(MessageBuilder.withPayload(
                    "<MessageBody><NoSuchBean>This bean does not exist</NoSuchBean></MessageBody>").build());
            Assert.fail("Missing exception due to unknown message handler");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchBeanDefinitionException);
        }
    }
}
