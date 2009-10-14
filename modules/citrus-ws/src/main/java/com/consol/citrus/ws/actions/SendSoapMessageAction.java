/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

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
