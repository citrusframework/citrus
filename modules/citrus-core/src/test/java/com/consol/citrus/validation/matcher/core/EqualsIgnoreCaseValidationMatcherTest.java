package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class EqualsIgnoreCaseValidationMatcherTest extends AbstractTestNGUnitTest {
    
    EqualsIgnoreCaseValidationMatcher matcher = new EqualsIgnoreCaseValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "VALUE", "value");
        matcher.validate("field", "VALUE", "VALUE");
        matcher.validate("field", "value", "VALUE");
        matcher.validate("field", "value", "value");
        matcher.validate("field", "$%& value 123", "$%& VALUE 123");
        matcher.validate("field", "/() VALUE ŠšŸ", "/() VALUE €…†");
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "VALUE", "VAIUE");
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
