package org.citrusframework.util;

import java.util.ArrayList;

import org.springframework.util.MultiValueMap;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SpringBeanTypeConverterTest {

    private final SpringBeanTypeConverter converter = SpringBeanTypeConverter.INSTANCE;

    @Test
    public void testConverter() {
        Assert.assertEquals(converter.convertIfNecessary("{key=[value]}", MultiValueMap.class).getFirst("key"), new String[] {"value"});
        Assert.assertEquals(converter.convertIfNecessary("{key=[value1,value2]}", MultiValueMap.class).get("key").getClass(), ArrayList.class);
        Assert.assertEquals(converter.convertIfNecessary("{key=[value1,value2]}", MultiValueMap.class).getFirst("key"), new String[] {"value1", "value2"});
    }
}
