package com.consol.citrus.actions;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Bean to print out variable values. Bean requires a list of variable
 * names that will bew printed to the console with its according value.
 * If the value happens to be null the test failes.
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class InfoBean extends AbstractTestAction {
    /** Values to be validated */
    private List infoValues;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(InfoBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        boolean isSuccess = true;

        Iterator it;
        if (infoValues != null && infoValues.size() > 0) {
            log.info("Validating variables using custom map:");
            
            it = infoValues.iterator();
        } else {
            log.info("Validating all variables in context:");
            
            it = context.getVariables().keySet().iterator();
        }

        while (it.hasNext()) {
            String key = (String)it.next();
            String value = context.getVariable(key);

            log.info("Current value of variable " + key + " = " + value);

            if (value == null)  {
                log.info("Validation error: Variable value is null");
                
                isSuccess = false;
            }
        }

        if (!isSuccess) {
            throw new CitrusRuntimeException("Validation error, because one or more variables are null");
        }
    }

    /**
     * Setter for info values list
     * @param infoValues
     */
    public void setInfoValues(List infoValues) {
        this.infoValues = infoValues;
    }
}
