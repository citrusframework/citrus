package com.consol.citrus.actions;

import java.text.ParseException;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class FailAction extends AbstractTestAction {

    private String message = "Generated error to interrupt test execution";

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
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
