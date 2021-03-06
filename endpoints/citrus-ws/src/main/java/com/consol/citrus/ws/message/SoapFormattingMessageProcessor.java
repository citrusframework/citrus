/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.ws.message;

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.XmlFormattingMessageProcessor;

/**
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class SoapFormattingMessageProcessor extends XmlFormattingMessageProcessor {

    @Override
    public void processMessage(Message message, TestContext context) {
        if (message instanceof SoapFault) {
            List<String> faultDetailsFormat = new ArrayList<>();
            for (String faultDetail : ((SoapFault) message).getFaultDetails()) {
                faultDetailsFormat.add(XMLUtils.prettyPrint(faultDetail));
            }

            if (faultDetailsFormat.size() > 0) {
                ((SoapFault) message).faultDetails(faultDetailsFormat);
            }
        }

        super.processMessage(message, context);
    }
}
