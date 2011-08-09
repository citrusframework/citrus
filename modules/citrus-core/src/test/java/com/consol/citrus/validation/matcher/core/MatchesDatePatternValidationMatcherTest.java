package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class MatchesDatePatternValidationMatcherTest extends AbstractTestNGUnitTest {
    
	MatchesDatePatternValidationMatcher matcher = new MatchesDatePatternValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
    	matcher.validate("field", "2011-10-10", "yyyy-MM-dd");
        matcher.validate("field", "10.10.2011", "dd.MM.yyyy");
        matcher.validate("field", "2011-01-01T01:02:03", "yyyy-MM-dd'T'HH:mm:ss");
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "201110-10", "yy-MM-dd");
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
