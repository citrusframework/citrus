package com.consol.citrus.validation.matcher.core;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcher;

/**
 * ValidationMatcher based on String.contains()
 * 
 * @author Christian Wied
 */
public class ContainsValidationMatcher implements ValidationMatcher {

    public void validate(String fieldName, String value, String control) throws ValidationException {
        
        if (!value.contains(control)) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + value
                    + "', control value is '" + control + "'.");
        }
    }
}
