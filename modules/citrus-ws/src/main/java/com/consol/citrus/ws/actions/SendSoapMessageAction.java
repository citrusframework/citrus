/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ws.actions;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Message send action able to add SOAP attachment support to normal message sending action.
 *  
 * @author Christoph Deppisch
 */
public class SendSoapMessageAction extends SendMessageAction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SendSoapMessageAction.class);

    /** SOAP attachments */
    private List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();

    /** enable/disable mtom attachments */
    private boolean mtomEnabled = false;

    /** Marker for inline mtom binary data */
    public static final String CID_MARKER = "cid:";

    @Override
    protected SoapMessage createMessage(TestContext context, String messageType) {
        Message message = super.createMessage(context, getMessageType());

        SoapMessage soapMessage = new SoapMessage(message).mtomEnabled(mtomEnabled);
        try {
            for (SoapAttachment attachment : attachments) {
                attachment.setTestContext(context);

                if (mtomEnabled) {
                    String messagePayload = soapMessage.getPayload(String.class);
                    String cid = CID_MARKER + attachment.getContentId();

                    if (attachment.isMtomInline() && messagePayload.contains(cid)) {
                        byte[] attachmentBinaryData = FileUtils.readToString(attachment.getInputStream(), Charset.forName(attachment.getCharsetName())).getBytes(Charset.forName(attachment.getCharsetName()));
                        if (attachment.getEncodingType().equals(SoapAttachment.ENCODING_BASE64_BINARY)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Adding inline base64Binary data for attachment: %s", cid);
                            }
                            messagePayload = messagePayload.replaceAll(cid, Base64.encodeBase64String(attachmentBinaryData));
                        } else if (attachment.getEncodingType().equals(SoapAttachment.ENCODING_HEX_BINARY)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Adding inline hexBinary data for attachment: %s", cid);
                            }
                            messagePayload = messagePayload.replaceAll(cid, Hex.encodeHexString(attachmentBinaryData).toUpperCase());
                        } else {
                            throw new CitrusRuntimeException(String.format("Unsupported encoding type '%s' for SOAP attachment: %s - choose one of %s or %s",
                                    attachment.getEncodingType(), cid, SoapAttachment.ENCODING_BASE64_BINARY, SoapAttachment.ENCODING_HEX_BINARY));
                        }
                    } else {
                        messagePayload = messagePayload.replaceAll(cid, String.format("<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"%s\"/>", cid));
                        soapMessage.addAttachment(attachment);
                    }

                    soapMessage.setPayload(messagePayload);
                } else {
                    soapMessage.addAttachment(attachment);
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        return soapMessage;
    }

    /**
     * Gets the attachments.
     * @return the attachments
     */
    public List<SoapAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the control attachments.
     * @param attachments the control attachments
     */
    public SendSoapMessageAction setAttachments(List<SoapAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }
    
    /**
     * Enable or disable mtom attachments
     * @param mtomEnabled
     */
    public SendSoapMessageAction setMtomEnabled(boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
        return this;
    }

    /**
     * Gets mtom attachments enabled
     * @return 
     */
    public boolean getMtomEnabled() {
        return this.mtomEnabled;
    }
}
