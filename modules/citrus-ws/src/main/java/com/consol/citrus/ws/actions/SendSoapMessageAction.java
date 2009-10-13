package com.consol.citrus.ws.actions;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ws.message.WebServiceMessageSender;

public class SendSoapMessageAction extends SendMessageAction {

    private Resource attachment;
    
    private String contentType = "text/plain";
    
    private String contentId = "SOAPAttachment";
    
    @Override
    public void execute(TestContext context) {
        Message<?> message = createMessage(context);
        
        if(messageSender instanceof WebServiceMessageSender == false) {
            throw new CitrusRuntimeException("Sending SOAP messages requires a " +
            		"'com.consol.citrus.ws.message.WebServiceMessageSender' but was '" + message.getClass().getName() + "'");
        }
        
        ((WebServiceMessageSender)messageSender).send(message, attachment, contentId, contentType);
    }

    /**
     * @param attachment the attachment to set
     */
    public void setAttachment(Resource attachment) {
        this.attachment = attachment;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}
