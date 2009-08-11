package com.consol.citrus.actions;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Bean used for time measurement during test workflow
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class TimeWatcherBean extends AbstractTestAction {

    /** Static member to hold all time stamps */
    private static Map timeStamps = new HashMap();

    /** Default time stamp id */
    private static String DEFAULT_ID = "DEFAULT_TIME";

    /** Id of the time measurement */
    private String id = DEFAULT_ID;

    /** Description of time line */
    private String description;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TimeWatcherBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        DecimalFormat decFormat = new DecimalFormat("0.0");
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);

        try {
            if (timeStamps.containsKey(id)) {
                if (description != null)
                    log.info("TimeWatcher " + id + " after " + decFormat.format((System.currentTimeMillis() - ((Long)timeStamps.get(id)).longValue())/(double)1000) + " seconds (" + description + ")");
                else
                    log.info("TimeWatcher " + id + " after " + decFormat.format((System.currentTimeMillis() - ((Long)timeStamps.get(id)).longValue())/(double)1000) + " seconds");
            } else {
                log.info("Starting TimeWatcher: " + id);
                timeStamps.put(id, Long.valueOf(System.currentTimeMillis()));
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Setter for id
     * @param period
     */
    public void setId(String period) {
        this.id = period;
    }

    /**
     * Setter for description
     * @param description
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
