package org.citrusframework.validation;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class HeaderValidatorTest {

    @Test
    public void testLookup() {
        Map<String, HeaderValidator> validators = HeaderValidator.lookup();
        Assert.assertEquals(validators.size(), 1L);
        Assert.assertNotNull(validators.get("defaultHeaderValidator"));
        Assert.assertEquals(validators.get("defaultHeaderValidator").getClass(), DefaultHeaderValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(HeaderValidator.lookup("default").isPresent());
    }
}
