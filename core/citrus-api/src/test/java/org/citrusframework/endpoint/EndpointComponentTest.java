package org.citrusframework.endpoint;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class EndpointComponentTest {

    @Test
    public void testLookup() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 0L);
    }
}
