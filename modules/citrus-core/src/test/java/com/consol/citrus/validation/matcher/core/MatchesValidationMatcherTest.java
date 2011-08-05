package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class MatchesValidationMatcherTest extends AbstractTestNGUnitTest {
    
	MatchesValidationMatcher matcher = new MatchesValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
    	matcher.validate("field", "This is a test", ".*");
        matcher.validate("field", "This is a test", "Thi.*");
        matcher.validate("field", "This is a test", ".*test");
        matcher.validate("field", "aaaab", "a*b");
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "a", "[^a]");
    	assertException("field", "aaaab", "aaab*");
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
