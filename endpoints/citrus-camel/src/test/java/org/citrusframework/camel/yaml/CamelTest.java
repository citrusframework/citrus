package org.citrusframework.camel.yaml;

import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CamelTest {

    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(YamlTestActionBuilder.lookup().containsKey("camel"));
        Assert.assertTrue(YamlTestActionBuilder.lookup("camel").isPresent());
        Assert.assertEquals(YamlTestActionBuilder.lookup("camel").get().getClass(), Camel.class);
    }
}
