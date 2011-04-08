/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.http.controller;

import java.util.*;
import java.util.Map.Entry;

import org.springframework.http.*;
import org.springframework.integration.Message;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.adapter.handler.EmptyResponseProducingMessageHandler;
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.MessageUtils;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/*")
public class HttpMessageController {

    /** Message handler for incoming requests, providing proper responses */
    private MessageHandler messageHandler = new EmptyResponseProducingMessageHandler();
    
    /** Header mapper */
    private HeaderMapper<HttpHeaders> headerMapper = DefaultHttpHeaderMapper.inboundMapper();
    
    /** Default charset for response generation */
    private String charset = "UTF-8";
    
    /** Default content type for response generation */
    private String contentType = "text/plain";
    
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT, RequestMethod.GET, RequestMethod.DELETE })
    @ResponseBody
    public ResponseEntity<String> handleRequest(HttpEntity<String> requestEntity) {
        Map<String, ?> httpRequestHeaders = headerMapper.toHeaders(requestEntity.getHeaders());
        Map<String, String> customHeaders = new HashMap<String, String>();
        for (Entry<String, List<String>> header : requestEntity.getHeaders().entrySet()) {
            if (!httpRequestHeaders.containsKey(header.getKey())) {
                customHeaders.put(header.getKey(), StringUtils.collectionToCommaDelimitedString(header.getValue()));
            }
        }
        
        Message<?> response = messageHandler.handleMessage(MessageBuilder.withPayload(requestEntity.getBody())
                                                                         .copyHeaders(httpRequestHeaders)
                                                                         .copyHeaders(customHeaders)
                                                                         .build());
        
        ResponseEntity<String> responseEntity = generateResponse(response);
        return responseEntity;
    }
    
    /**
     * Generates the Http response message from given Spring Integration message.
     * @param response
     * @return
     */
    private ResponseEntity<String> generateResponse(Message<?> responseMessage) {
        if (responseMessage == null) {
            return new ResponseEntity<String>(HttpStatus.OK);
        }
        
        HttpHeaders httpHeaders = new HttpHeaders();
        headerMapper.fromHeaders(responseMessage.getHeaders(), httpHeaders);
        
        Map<String, ?> messageHeaders = responseMessage.getHeaders();
        for (Entry<String, ?> header : messageHeaders.entrySet()) {
            if (!header.getKey().startsWith(CitrusMessageHeaders.PREFIX) && 
                    !MessageUtils.isSpringInternalHeader(header.getKey()) &&
                    !httpHeaders.containsKey(header.getKey())) {
                httpHeaders.add(header.getKey(), header.getValue().toString());
            }
        }
        
        if (httpHeaders.getContentType() == null) {
            httpHeaders.setContentType(MediaType.parseMediaType(contentType.contains("charset") ? contentType : contentType + ";charset=" + charset));
        }
        
        HttpStatus status = HttpStatus.OK;
        if (responseMessage.getHeaders().containsKey(CitrusHttpMessageHeaders.HTTP_STATUS_CODE)) {
            status = HttpStatus.valueOf(Integer.valueOf(responseMessage.getHeaders().get(CitrusHttpMessageHeaders.HTTP_STATUS_CODE).toString()));
        }
        
        return new ResponseEntity<String>(responseMessage.getPayload().toString(), httpHeaders, status);
    }

    /**
     * Sets the messageHandler.
     * @param messageHandler the messageHandler to set
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Sets the headerMapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(HeaderMapper<HttpHeaders> headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Sets the charset.
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Sets the contentType.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
