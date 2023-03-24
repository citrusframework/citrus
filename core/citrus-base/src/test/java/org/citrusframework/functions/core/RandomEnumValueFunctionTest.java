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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test the {@link RandomEnumValueFunction} function.
 *
 * @author Dimo Velev (dimo.velev@gmail.com)
 *
 */
public class RandomEnumValueFunctionTest extends UnitTestSupport {
	private final Random random = new Random(System.currentTimeMillis());

	private List<String> generateRandomValues() {
		final int valueCount = random.nextInt(15) + 5;
		final List<String> values = new ArrayList<String>(valueCount);
		for (int i=0; i<valueCount; i++) {
			values.add("value" + i);
		}
		return values;
	}

	@Test
	public void testWithParameters() {
		RandomEnumValueFunction testee = new RandomEnumValueFunction();
		final List<String> values = generateRandomValues();
		for (int i=0; i<100; i++) {
			final String value = testee.execute(values, context);
			assertNotNull(value);
			assertTrue(values.contains(value));
		}
	}

	@Test
	public void testWithValues() {
		RandomEnumValueFunction testee = new RandomEnumValueFunction();
		testee.setValues(generateRandomValues());
		final List<String> noParameters = Collections.emptyList();

		for (int i=0; i<100; i++) {
			final String value = testee.execute(noParameters, context);
			assertNotNull(value);
			assertTrue(testee.getValues().contains(value));
		}
	}

	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
	public void testWithBoth() {
		RandomEnumValueFunction testee = new RandomEnumValueFunction();
		testee.setValues(generateRandomValues());
		final List<String> params = generateRandomValues();
		testee.execute(params, context);
	}

	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
	public void testWithNone() {
		RandomEnumValueFunction testee = new RandomEnumValueFunction();
		final List<String> noParameters = Collections.emptyList();
		testee.execute(noParameters, context);
	}
}
