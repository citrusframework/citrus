package com.consol.citrus.service;

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.Message;

/**
 * Service interface for all services
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public interface Service {
    /**
     * Send message to service destination
     * @param message xml to be sent
     * @throws TestSuiteException
     */
    public void sendMessage(Message message) throws TestSuiteException;

    /**
     * Receive message from service destination
     * @return message as xml string
     * @throws TestSuiteException
     */
    public Message receiveMessage() throws TestSuiteException;

    /**
     * Returns the current service destination
     * @return service destination
     */
    public String getServiceDestination();

    /**
     * Changes the service destination during runtime
     * @param destination the new service destination
     * @throws TestSuiteException
     */
    public void changeServiceDestination(String destination) throws TestSuiteException;
}
