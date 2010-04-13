/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

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
