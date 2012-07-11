/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.admin.controller;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.service.AppContextHolder;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.server.Server;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/context")
public class AppContextController {

    @Autowired
    private AppContextHolder appContextHolder;
    
    @RequestMapping(method = { RequestMethod.GET })
    @ResponseBody
    public AppContextInfo startContext(HttpEntity<String> requestEntity) {
        AppContextInfo appContextInfo = new AppContextInfo();
        
        ApplicationContext ctx = appContextHolder.getApplicationContext();
        Map<String, MessageSender> senders = ctx.getBeansOfType(MessageSender.class);
        
        for (Entry<String, MessageSender> sender : senders.entrySet()) {
            MessageSenderType senderType = new MessageSenderType();
            senderType.setName(sender.getKey());
            appContextInfo.getMessageSenders().add(senderType);
        }
        
        Map<String, MessageReceiver> receivers = ctx.getBeansOfType(MessageReceiver.class);
        
        for (Entry<String, MessageReceiver> receiver : receivers.entrySet()) {
            MessageReceiverType receiverType = new MessageReceiverType();
            receiverType.setName(receiver.getKey());
            appContextInfo.getMessageReceivers().add(receiverType);
        }
        
        Map<String, Server> servers = ctx.getBeansOfType(Server.class);
        
        for (Entry<String, Server> server : servers.entrySet()) {
            ServerInstanceType serverType = new ServerInstanceType();
            serverType.setName(server.getKey());
            appContextInfo.getServerInstances().add(serverType);
        }
        
        return appContextInfo;
    }
    
    @RequestMapping(method = { RequestMethod.DELETE })
    @ResponseBody
    public ResponseEntity<?> stopContext(HttpEntity<String> requestEntity) {
        appContextHolder.destroyApplicationContext();
        
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
