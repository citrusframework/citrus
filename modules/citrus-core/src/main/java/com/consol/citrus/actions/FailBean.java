package com.consol.citrus.actions;

import java.text.ParseException;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class FailBean extends AbstractTestAction {

    private String message = "Generated error to interrupt test execution";

    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        try {
            throw new CitrusRuntimeException(context.replaceDynamicContentInString(message));
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
