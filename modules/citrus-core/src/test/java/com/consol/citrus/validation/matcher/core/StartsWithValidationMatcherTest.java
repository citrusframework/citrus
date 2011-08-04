package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class StartsWithValidationMatcherTest extends AbstractTestNGUnitTest {
    
	StartsWithValidationMatcher matcher = new StartsWithValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
    	matcher.validate("field", "This is a test", "");
        matcher.validate("field", "This is a test", "T");
        matcher.validate("field", "This is a test", "This ");
        matcher.validate("field", "This is a test", "This is ");
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "This is a test", "his");
    	assertException("field", "This is a test", "test");
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
