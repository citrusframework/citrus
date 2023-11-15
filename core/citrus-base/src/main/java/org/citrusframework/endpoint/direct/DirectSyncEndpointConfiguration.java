package org.citrusframework.endpoint.direct;

import org.citrusframework.endpoint.PollableEndpointConfiguration;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncEndpointConfiguration extends DirectEndpointConfiguration implements PollableEndpointConfiguration {

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval to get.
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
