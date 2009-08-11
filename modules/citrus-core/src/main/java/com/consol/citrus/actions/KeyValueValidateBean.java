package com.consol.citrus.actions;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;

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
     * The service with which the message is beeing sent or received.
     */
    protected MessageReceiver messageReceiver;

    /**
     * The text ressource as a inline definition within
     * the spring application context (testContext.xml).
     */
    protected String textData;
    
    private long receiveTimeout = 5000L;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(KeyValueValidateBean.class);

    /**
     * Following actions will be executed:
     * <p>1. The message is received.
     * <p>2. The received message is validated against the source message
     * @throws CitrusRuntimeException
     * @return boolean <tt>true</tt> if successful
     */
    @Override
    public void execute(TestContext context) {
        HashMap receivingMap = null;
        HashMap contentMap = null;

        /** 1. The message is received and the header properties for each key
         * within getHeaderValues are read into the
         * corresponding variables.
         */
        Message receivedMessage = messageReceiver.receive(receiveTimeout);

        if (receivedMessage == null)
            throw new CitrusRuntimeException("Received message is null!");

        if(log.isDebugEnabled()) {
            log.debug("Received message " + receivedMessage.getPayload());
        }
        
        log.info("TestData" + textData);

        receivingMap = createHashMap(receivedMessage.getPayload().toString());
        contentMap = createHashMap(textData);

        if (!receivingMap.equals(contentMap))
            throw new CitrusRuntimeException("Key value validation failed!");
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
     * @param textData the textData to set
     */
    public void setTextData(String textData) {
        this.textData = textData;
    }

    /**
     * @param messageReceiver the messageReceiver to set
     */
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
}