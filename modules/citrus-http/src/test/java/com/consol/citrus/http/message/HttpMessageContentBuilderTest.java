
package com.consol.citrus.http.message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.consol.citrus.validation.builder.StaticMessageContentBuilder;

/**
 * HttpMessageContentBuilderTest implements unit tests for
 * {@link HttpMessageContentBuilder}.
 * 
 * @author Muhammad Ali Qasmi
 * @since 2.7.3
 */
public class HttpMessageContentBuilderTest {

    private HttpMessageContentBuilder messageBuilder;

    @Before
    public void setUp() throws Exception {
        // Prepares test data
        MultiValueMap<String,Object> payload = new LinkedMultiValueMap<String,Object>();
        payload.add("key1", "value1");
        payload.add("key2", "value2");
        payload.add("key3", "value3");
        
        // Initializes message builder 
        HttpMessage message = new HttpMessage(payload);
        messageBuilder = new HttpMessageContentBuilder(message, new StaticMessageContentBuilder(message));
    }

    @Test
    public void testToBuildHttpMessageWithMultiValueMap() {
        Object payload = messageBuilder.buildMessagePayload(null,null);
        
        if(payload instanceof MultiValueMap<?,?>) {
            MultiValueMap<String,Object> map = (MultiValueMap<String, Object>) payload;
            assertEquals(map.size(), 3);
            assertEquals(map.get("key1").get(0), "value1");
            assertEquals(map.get("key2").get(0), "value2");
            assertEquals(map.get("key3").get(0), "value3");
        }
    }

}
