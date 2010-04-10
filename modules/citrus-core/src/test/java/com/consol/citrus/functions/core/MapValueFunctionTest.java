package com.consol.citrus.functions.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;

/**
 * Test the {@link MapValueFunction} function.
 * @author Dimo Velev (dimo.velev@gmail.com)
 *
 */
public class MapValueFunctionTest {
	private Map<String, String> map = null;
	
	@BeforeTest
	public void init() {
		map = new HashMap<String, String>();
		
		map.put("401", "Unauthorized");
		map.put("200", "OK");
		map.put("500", "Internal Server Error");
	}
	
	@Test(expectedExceptions = {IllegalArgumentException.class})
	public void testNoMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.afterPropertiesSet();
	}
	
	@Test
	public void testMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.setMap(map);
		testee.afterPropertiesSet();
		for(String key : map.keySet()) {
			String result = testee.execute(Arrays.asList(key));
			Assert.assertEquals(result, map.get(key));
		}
	}
	
	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
	public void testMissingMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.setMap(map);
		testee.afterPropertiesSet();
		Assert.assertFalse(map.containsKey("303"));
		testee.execute(Arrays.asList("303"));
	}
}
