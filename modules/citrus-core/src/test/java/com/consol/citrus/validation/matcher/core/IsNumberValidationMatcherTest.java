package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class IsNumberValidationMatcherTest extends AbstractTestNGUnitTest {
    
	IsNumberValidationMatcher matcher = new IsNumberValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
    	// control is irrelevant here
        matcher.validate("field", "2", "3");
        matcher.validate("field", "-1", "1");
        matcher.validate("field", "-0.000000001", "0");
        matcher.validate("field", "0", "aaa");
        matcher.validate("field", "1E+07", "aaa");
        matcher.validate("field", "1E-7", "aaa");
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "NaN", "2");
    	assertException("field", "2a", "NaN");
    	assertException("field", "a2.0", "2.0");
    	assertException("field", "2.1A+07", "2.0");
    }

    private void assertException(String fieldName, String value, String control) {
    	try {
    		matcher.validate(fieldName, value, control);
    		Assert.fail("Expected exception not thrown!");
    	} catch (ValidationException e) {
			Assert.assertTrue(e.getMessage().contains(fieldName));
			Assert.assertTrue(e.getMessage().contains(value));
		}
    }
}
