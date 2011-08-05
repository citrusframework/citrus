package com.consol.citrus.validation.matcher.core;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcher;

/**
 * ValidationMatcher based on Double > Double.
 * 
 * @author Christian Wied
 */
public class IsNumberValidationMatcher implements ValidationMatcher {

    public void validate(String fieldName, String value, String control) throws ValidationException {
        
    	Double dValue;
    	try {
    		dValue = Double.parseDouble(value);
    	} catch (Exception e) {
    		throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + value
                    + "' and is not a number.");
		}
    	if (dValue.isNaN() || dValue.isInfinite()) {
    		throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + value
                    + "' and not a number.");
    	}
    }
}
