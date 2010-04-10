package com.consol.citrus.functions.core;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;

/**
 * Test the {@link RandomEnumValueFunction} function.
 * 
 * @author Dimo Velev (dimo.velev@gmail.com)
 *
 */
public class RandomEnumValueFunctionTest {
	private Random random = new Random(System.currentTimeMillis());
	
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
		for(int i=0; i<100; i++) {
			final String value = testee.execute(values);
			assertNotNull(value);
			assertTrue(values.contains(value));
		}
	}
	
	@Test
	public void testWithValues() {
		RandomEnumValueFunction testee = new RandomEnumValueFunction();
		testee.setValues(generateRandomValues());
		final List<String> noParameters = Collections.emptyList();
		
		for(int i=0; i<100; i++) {
			final String value = testee.execute(noParameters);
			assertNotNull(value);
			assertTrue(testee.getValues().contains(value));
		}
	}
	
	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
	public void testWithBoth() {
		RandomEnumValueFunction testee = new RandomEnumValueFunction();
		testee.setValues(generateRandomValues());
		final List<String> params = generateRandomValues();
		testee.execute(params);
	}
	
	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
	public void testWithNone() {
		RandomEnumValueFunction testee = new RandomEnumValueFunction();
		final List<String> noParameters = Collections.emptyList();
		testee.execute(noParameters);
	}
}
