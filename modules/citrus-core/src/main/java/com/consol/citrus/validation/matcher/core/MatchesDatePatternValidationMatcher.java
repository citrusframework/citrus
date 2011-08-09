package com.consol.citrus.validation.matcher.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.PatternSyntaxException;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcher;

/**
 * ValidationMatcher checking for valid date format.
 * 
 * @author Christian Wied
 */
public class MatchesDatePatternValidationMatcher implements ValidationMatcher {

    public void validate(String fieldName, String value, String control) throws ValidationException {
        
    	SimpleDateFormat dateFormat;
    	try {
    		dateFormat = new SimpleDateFormat(control);
    	} catch (PatternSyntaxException e) {
    		throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Control value has invalid syntax: " + e.getMessage());
		}
    	try {
			dateFormat.parse(value);
		} catch (ParseException e) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + value
                    + "', control value is '" + control + "'. Exception: " + e.getMessage());
		}
    }
}
