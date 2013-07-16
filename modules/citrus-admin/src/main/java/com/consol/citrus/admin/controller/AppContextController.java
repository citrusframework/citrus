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

import com.consol.citrus.admin.executor.ApplicationContextHolder;
import com.consol.citrus.admin.model.*;
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
    private ApplicationContextHolder appContextHolder;

    @RequestMapping(value="/status", method = RequestMethod.GET)
    @ResponseBody
    public Boolean getStatus() {
        return appContextHolder.isApplicationContextLoaded();
    }

    @RequestMapping(method = { RequestMethod.GET })
    public void startContext(HttpEntity<String> requestEntity) {
        appContextHolder.loadApplicationContext();
    }
    
    @RequestMapping(method = { RequestMethod.DELETE })
    @ResponseBody
    public ResponseEntity<?> stopContext(HttpEntity<String> requestEntity) {
        appContextHolder.destroyApplicationContext();
        
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
