
package com.consol.citrus.http.message;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.validation.builder.StaticMessageContentBuilder;

/**
 * HttpMessageContentBuilderTest implements unit tests for
 * {@link HttpMessageContentBuilder}.
 * 
 * @author Muhammad Ali Qasmi
 * @since 2.7.3
 */
public class HttpMessageContentBuilderTest extends AbstractTestNGCitrusTest {

    private HttpMessageContentBuilder messageBuilder;

    @Test
    public void testToBuildHttpMessageWithMultiValueMap() {
        
        // Prepares test data
        MultiValueMap<String,Object> expectedPayload = new LinkedMultiValueMap<String,Object>();
        expectedPayload.add("key1", "value1");
        expectedPayload.add("key2", "value2");
        expectedPayload.add("key3", "value3");
        // Initializes message builder 
        HttpMessage message = new HttpMessage(expectedPayload);
        
        messageBuilder = new HttpMessageContentBuilder(message, new StaticMessageContentBuilder(message));
        Object actualPayload = messageBuilder.buildMessagePayload(null,null);
        
        if(actualPayload instanceof MultiValueMap<?,?>) {
            MultiValueMap<String,Object> map = (MultiValueMap<String, Object>) actualPayload;
            Assert.assertEquals(map.size(), 3);
            Assert.assertEquals(map.get("key1").get(0), "value1");
            Assert.assertEquals(map.get("key2").get(0), "value2");
            Assert.assertEquals(map.get("key3").get(0), "value3");
        }
    }

}
