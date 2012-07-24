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

package com.consol.citrus.samples.bookregistry;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.MessageHeaders;
import org.springframework.oxm.castor.CastorMarshaller;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;


import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.actions.TraceTimeAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.samples.CitrusSamplesDemo;
import com.consol.citrus.samples.bookregistry.model.*;
import com.consol.citrus.samples.common.DemoAwareTestNGCitrusTestBuilder;
import com.consol.citrus.validation.MarshallingValidationCallback;

/**
 * @author Christoph Deppisch
 */
public class AddBook_Ok_2_Test extends DemoAwareTestNGCitrusTestBuilder {

    private BookRegistryDemo demo = new BookRegistryDemo();
    
    @Autowired
    @Qualifier("bookRegistryRequestMessageSender")
    private MessageSender bookRequestMessageSender;
    
    @Autowired
    @Qualifier("bookRegistryResponseMessageHandler")
    private MessageReceiver bookResponseMessageHandler;
    
    @Autowired
    private CastorMarshaller marshaller;
    
    @Test
    public void runTest(ITestContext testContext) throws ClassNotFoundException {
        String isbn = "978-citrus:randomNumber(10)";
        
//        send()
//            .with(bookRequestMessageSender)
//            .payload(createAddBookRequestMessage(isbn), marshaller)
//            .header("citrus_soap_action", "addBook");
//        
//        receive()
//            .with(bookResponseMessageHandler)
//            .validationCallback(new MarshallingValidationCallback<AddBookResponseMessage>(marshaller) {
//                @Override
//                public void validate(AddBookResponseMessage response, MessageHeaders headers) {
//                    Assert.assertTrue(response.isSuccess());
//                }
//            });      
//        traceTime("Bulbasaur");
//        sleep(2.0);
//        traceTime("Bulbasaur");
//        traceTime("Pikachu");
//        sleep(1.5);
//        traceTime("Pikachu");
//        traceTime("Bulbasaur");
//        traceTime();
//        sleep(3.0);
//        traceTime("Pikachu");
//        traceTime("Bulbasaur");
//        traceTime();
        input().message("Gib was ein: ").variable("INPUT").validAnswer("JA", "NEIN", "VIELLEICHT");
        echo("${INPUT}");
        

       
        createVariables()
            	.add("Var1", "124452")
            	.add("Var2","3345346")
            	.add("isbn",  "citrus:randomNumber(10)");
            	;
            
         traceVariables()
            	.trace("Var1")
            	.trace("isbn")
            	;
         

        
        //catchException("com.consol.citrus.exceptions.CitrusRuntimeException", traceVariables().trace(isbn), traceVariables().trace("Var1"));
        executeTest(testContext);
        
    }
    
    /**
     * @param isbn
     * @return
     */
    private AddBookRequestMessage createAddBookRequestMessage(String isbn) {
        AddBookRequestMessage requestMessage = new AddBookRequestMessage();
        Book book = new Book();
        book.setAuthor("Mike Loukides, Sonatype");
        book.setTitle("Maven: The Definitive Guide");
        book.setIsbn(isbn);
        book.setYear(2008);
        book.setRegistrationDate(new Date());
        requestMessage.setBook(book);
        return requestMessage;
    }
    
    /**
     * Gets the demo application.
     * @return
     */
    public CitrusSamplesDemo getDemo() {
        return demo;
    }

}
