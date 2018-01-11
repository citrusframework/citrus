/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.functions.core;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Test the {@link MapValueFunction} function.
 * @author Dimo Velev (dimo.velev@gmail.com)
 *
 */
public class MapValueFunctionTest extends AbstractTestNGUnitTest {
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
		for (String key : map.keySet()) {
			String result = testee.execute(Arrays.asList(key), context);
			Assert.assertEquals(result, map.get(key));
		}
	}
	
	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
	public void testMissingMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.setMap(map);
		testee.afterPropertiesSet();
		Assert.assertFalse(map.containsKey("303"));
		testee.execute(Arrays.asList("303"), context);
	}
}
