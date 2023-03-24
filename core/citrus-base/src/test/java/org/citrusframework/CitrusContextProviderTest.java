package org.citrusframework;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CitrusContextProviderTest {

    @Test
    public void testLookup() {
        CitrusContextProvider provider = CitrusContextProvider.lookup();
        Assert.assertEquals(provider.create().getClass(), CitrusContext.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertFalse(CitrusContextProvider.lookup(CitrusContextProvider.SPRING).isPresent());
    }

}
