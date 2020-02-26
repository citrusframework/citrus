package com.consol.citrus.endpoint;

import java.util.Collections;

import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointFactoryTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testResolveChannelEndpoint() throws Exception {
        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(Collections.emptyMap());
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("channel:channel.name", context);

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);
        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channel.name");
    }
}
