package com.consol.citrus.actions;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.message.Message;
import com.consol.citrus.service.Service;
import com.consol.citrus.variable.VariableUtils;

/**
 * This bean recieves a message on the given <tt>service</tt>
 * and validates the received values either against the given maps
 * <tt>validateValues</tt> and <tt>validateHeaderValues</tt>
 * or against a given template XML data (can be a file <tt>xmlRessource</tt>
 * or inline data <tt>xmlData</tt>. (- see spring application context)
 * <p>Elements within <tt>ignoreValues</tt> will not be validated, but if
 * the same element is also within the <tt>validateValues</tt> map it will
 * still be validated - thus <tt>validateValues</tt> takes precedence
 * over <tt>ignoreValues</tt>.
 * <p>Also values of the received message can be stored within variables.
 * This has to be defined in <tt>getMessageValues</tt> or <tt>getHeaderValues</tt>
 * - see ExecuteBean for detail.
 *
 * @author js Peter Frank Consol*GmbH 2006
 * @see RequestBean
 */
public class KeyValueValidateBean extends AbstractTestAction {

    /**
     * Destination to set before receiving
     */
    private String destination;

    /**
     * Destination to set before receiving
     */
    //    private HashMap ressceived;

    /**
     * The service with which the message is beeing sent or received.
     */
    protected Service service;

    /**
     * The text ressource as a inline definition within
     * the spring application context (testContext.xml).
     */
    protected String textData;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(KeyValueValidateBean.class);

    /**
     * Following actions will be executed:
     * <p>1. The message is received.
     * <p>2. The received message is validated against the source message
     * @return boolean <tt>true</tt> if successful
     * @throws TestSuiteException
     * @see ValidateSqlQueryBean
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        HashMap receivingMap = null;
        HashMap contentMap = null;

        //context.resetHeaderValues();

        if (destination != null) {
            String newDestination = null;

            if (VariableUtils.isVariableName(destination)) {
                newDestination = context.getVariable(destination);
            } else if(context.getFunctionRegistry().isFunction(destination)) {
                newDestination = FunctionUtils.resolveFunction(destination, context);
            } else {
                newDestination = destination;
            }

            if (newDestination != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting service destination to custom value " + newDestination);
                }
                service.changeServiceDestination(newDestination);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Setting service destination to custom value failed. Maybe variable is not set properly: " + destination);
                }
            }
        }

        /** 1. The message is received and the header properties for each key
         * within getHeaderValues are read into the
         * corresponding variables.
         */
        Message receivedMessage = service.receiveMessage();

        if (receivedMessage == null)
            throw new TestSuiteException("Received message is null!");

        if(log.isDebugEnabled()) {
            log.debug("Received message " + receivedMessage.getMessagePayload());
        }
        
        log.info("TestData" + textData);

        receivingMap = createHashMap(receivedMessage.getMessagePayload());
        contentMap = createHashMap(textData);

        if (!receivingMap.equals(contentMap))
            throw new TestSuiteException("Validation failed!");
    }

    private HashMap createHashMap(String msg) {
        HashMap map = new HashMap();
        //        int i = 0;
        String key,value="";

        StringTokenizer st = new StringTokenizer(msg);
        if (st.countTokens() < 3) {
            log.info("Invalid request");
            //TODO: Throw Exception
            return null;
        }
        value = st.nextToken();
        map.put("OrderId", value);
        value = st.nextToken();
        map.put("RequestTag", value);

        while (st.hasMoreTokens()) {
            key = st.nextToken();
            if (!st.hasMoreTokens()){
                log.info("Invalid request Missing value");
                //TODO: Throw Exception
            }
            value = st.nextToken();
            map.put(key, value);
        }
        return map;
    }

    /**
     * @param service the service to set
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * @param textData the textData to set
     */
    public void setTextData(String textData) {
        this.textData = textData;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }
}