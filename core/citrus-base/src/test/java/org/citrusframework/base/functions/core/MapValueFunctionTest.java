/*
 * Copyright the original author or authors.
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

package org.citrusframework.base.functions.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the {@link MapValueFunction} function.
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MapValueFunctionTest extends UnitTestSupport {
	private final Map<String, String> map = new HashMap<>();

	@BeforeAll
	public void init() {
		map.put("401", "Unauthorized");
		map.put("200", "OK");
		map.put("500", "Internal Server Error");
	}

	@Test
	public void testNoMapping() {
		MapValueFunction testee = new MapValueFunction();
		CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> testee.initialize());
		assertThat(thrown).hasMessage("MapValueFunction must not use an empty value map");
	}

	@Test
	public void testMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.setMap(map);
		for (String key : map.keySet()) {
			String result = testee.execute(Collections.singletonList(key), context);
			Assertions.assertEquals(map.get(key), result);
		}
	}

	@Test
	public void testMissingMapping() {
		MapValueFunction testee = new MapValueFunction();
		testee.setMap(map);
		Assertions.assertFalse(map.containsKey("303"));
		assertThrows(InvalidFunctionUsageException.class, () -> testee.execute(Collections.singletonList("303"), context));
	}
}
