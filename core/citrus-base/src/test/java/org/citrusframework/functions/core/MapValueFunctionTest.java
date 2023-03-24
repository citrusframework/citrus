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

package org.citrusframework.functions.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the {@link MapValueFunction} function.
 * @author Dimo Velev (dimo.velev@gmail.com)
 *
 */
public class MapValueFunctionTest extends UnitTestSupport {
	private final Map<String, String> map = new HashMap<>();

	@BeforeClass
	public void init() {
		map.put("401", "Unauthorized");
		map.put("200", "OK");
		map.put("500", "Internal Server Error");
	}

	@Test(expectedExceptions = {CitrusRuntimeException.class},
			expectedExceptionsMessageRegExp = "MapValueFunction must not use an empty value map")
	public void testNoMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.initialize();
	}

	@Test
	public void testMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.setMap(map);
		for (String key : map.keySet()) {
			String result = testee.execute(Collections.singletonList(key), context);
			Assert.assertEquals(result, map.get(key));
		}
	}

	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
	public void testMissingMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.setMap(map);
		Assert.assertFalse(map.containsKey("303"));
		testee.execute(Collections.singletonList("303"), context);
	}
}
