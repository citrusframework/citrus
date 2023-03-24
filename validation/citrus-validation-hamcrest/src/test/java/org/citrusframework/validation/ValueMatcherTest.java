package org.citrusframework.validation;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ValueMatcherTest {

    @Test
    public void testLookup() {
        Map<String, ValueMatcher> validators = ValueMatcher.lookup();
        Assert.assertEquals(validators.size(), 1L);
        Assert.assertNotNull(validators.get("hamcrest"));
        Assert.assertEquals(validators.get("hamcrest").getClass(), HamcrestValueMatcher.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(ValueMatcher.lookup("hamcrest").isPresent());
    }
}
