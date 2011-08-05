package com.consol.citrus.validation.matcher.core;

import java.util.regex.PatternSyntaxException;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcher;

/**
 * ValidationMatcher based on String.matches()
 * 
 * @author Christian Wied
 */
public class MatchesValidationMatcher implements ValidationMatcher {

    public void validate(String fieldName, String value, String control) throws ValidationException {
        
    	boolean success = false;
    	try {
    		success = value.matches(control);
    	} catch (PatternSyntaxException e) {
    		throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Control value has invalid syntax: " + e.getMessage());
		}
        if (!success) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + value
                    + "', control value is '" + control + "'.");
        }
    }
}
