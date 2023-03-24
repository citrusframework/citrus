package org.citrusframework;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CitrusSpringContextProviderTest {

    @Test
    public void testLookup() {
        CitrusContextProvider provider = CitrusContextProvider.lookup();
        Assert.assertEquals(provider.getClass(), CitrusSpringContextProvider.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(CitrusContextProvider.lookup(CitrusContextProvider.SPRING).isPresent());
    }
}
