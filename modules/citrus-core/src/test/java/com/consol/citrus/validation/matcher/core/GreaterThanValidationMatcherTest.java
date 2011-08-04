package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class GreaterThanValidationMatcherTest extends AbstractTestNGUnitTest {
    
	LowerThanValidationMatcher matcher = new LowerThanValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "2", "3");
        matcher.validate("field", "-1", "1");
        matcher.validate("field", "-0.000000001", "0");
        matcher.validate("field", "0", "0.000000001");
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "NaN", "2");
    	assertException("field", "2", "NaN");
    	assertException("field", "2.0", "2.0");
    	assertException("field", "2.1", "2.0");
    }

    private void assertException(String fieldName, String value, String control) {
    	try {
    		matcher.validate(fieldName, value, control);
    		Assert.fail("Expected exception not thrown!");
    	} catch (ValidationException e) {
			Assert.assertTrue(e.getMessage().contains(fieldName));
			Assert.assertTrue(e.getMessage().contains(value));
			Assert.assertTrue(e.getMessage().contains(control));
		}
    }
}
