/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.websocket;

import org.apache.log4j.*;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Special log4j logging appender pushes all logging events to logging web socket, so client
 * is getting all logging events for a test run from classpath.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class WebSocketLoggingAppender extends WriterAppender implements InitializingBean {

    @Autowired
    private LoggingWebSocket loggingWebSocket;

    /** Root logging appender - usually console appender */
    private Appender rootLogger;

    /** Process id of currently running test case */
    private String processId;

    @Override
    public void append(LoggingEvent event) {
        if (StringUtils.hasText(processId) && isAccepted(event)) {
            loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.LOG_MESSAGE, rootLogger.getLayout().format(event)));
        }
    }

    private boolean isAccepted(LoggingEvent event) {
        if (rootLogger.getFilter() == null) {
            return true;
        }

        return rootLogger.getFilter().decide(event) != Filter.DENY;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.rootLogger = Logger.getRootLogger().getAppender("CONSOLE");
        Logger.getRootLogger().addAppender(this);
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
