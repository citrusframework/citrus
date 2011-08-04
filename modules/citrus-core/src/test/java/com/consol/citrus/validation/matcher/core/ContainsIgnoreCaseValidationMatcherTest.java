package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class ContainsIgnoreCaseValidationMatcherTest extends AbstractTestNGUnitTest {
    
	ContainsIgnoreCaseValidationMatcher matcher = new ContainsIgnoreCaseValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "This is a test", "is a");
        matcher.validate("field", "This is a test", "this");
        matcher.validate("field", "This is a test", "TEST");
        matcher.validate("field", "This is a 0815test", "0815");
        matcher.validate("field", "This is a test", " ");
        matcher.validate("field", "This is a test", " IS A ");
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
