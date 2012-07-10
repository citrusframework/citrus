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

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.message.MessageSender;
import com.consol.citrus.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/*")
public class AdminController {

    @RequestMapping(value = "**", method = { RequestMethod.GET })
    @ResponseBody
    public ResponseEntity<?> handleRequest(HttpEntity<String> requestEntity) {
        StringBuilder builder = new StringBuilder();
        
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("citrus-context.xml");
        Map<String, MessageSender> senders = ctx.getBeansOfType(MessageSender.class);
        
        for (Entry<String, MessageSender> sender : senders.entrySet()) {
            builder.append(sender.getKey() + "<br>");
        }
        
        List<File> testFiles = FileUtils.getTestFiles("");
        
        for (File file : testFiles) {
            builder.append(file.getAbsolutePath() + "<br>");
        }
        
        return new ResponseEntity<String>(builder.toString(), HttpStatus.OK);
    }
}
