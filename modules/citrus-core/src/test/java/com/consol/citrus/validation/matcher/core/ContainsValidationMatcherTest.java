package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class ContainsValidationMatcherTest extends AbstractTestNGUnitTest {
    
    ContainsValidationMatcher matcher = new ContainsValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "This is a test", "is a");
        matcher.validate("field", "This is a test", "This");
        matcher.validate("field", "This is a test", "test");
        matcher.validate("field", "This is a 0815test", "0815");
        matcher.validate("field", "This is a test", " ");
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "This is a test", "0815");
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
