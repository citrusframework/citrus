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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.exceptions.ValidationException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class ValidateXmlCombinedJavaIT extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void validateXmlCombined() {
        variable("correlationId", "citrus:randomNumber(10)");      
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");
        
        echo("Test: Success with multiple validation mechanisms all together");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloResponseReceiver")
            .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>HelloService</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloResponse>")
            .validateScript("assert root.Text == 'Hello ${user}'")
            .validate("//pfx:HelloResponse/pfx:MessageId", "${messageId}")
            .validate("//pfx:HelloResponse/pfx:CorrelationId", "${correlationId}")
            .validate("//pfx:HelloResponse/pfx:Text", "citrus:concat('Hello ', ${user})")
            .namespace("pfx", "http://www.consol.de/schemas/samples/sayHello.xsd")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        echo("Test: Failure because of XML template data validation");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        assertException(
            receive("helloResponseReceiver")
                .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>Wrong User</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloResponse>")
                .validateScript("assert root.Text == 'Hello ${user}'")
                .validate("//pfx:HelloResponse/pfx:MessageId", "${messageId}")
                .validate("//pfx:HelloResponse/pfx:CorrelationId", "${correlationId}")
                .validate("//pfx:HelloResponse/pfx:Text", "citrus:concat('Hello ', ${user})")
                .namespace("pfx", "http://www.consol.de/schemas/samples/sayHello.xsd")
                .header("Operation", "sayHello")
                .header("CorrelationId", "${correlationId}")
        ).exception(ValidationException.class);
        
        echo("Test: Failure because of XML groovy script validation");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        assertException(
            receive("helloResponseReceiver")
                .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                               "<MessageId>${messageId}</MessageId>" +
                               "<CorrelationId>${correlationId}</CorrelationId>" +
                               "<User>HelloService</User>" +
                               "<Text>Hello ${user}</Text>" +
                           "</HelloResponse>")
                .validateScript("assert root.Text == 'Something else'")
                .validate("//pfx:HelloResponse/pfx:MessageId", "${messageId}")
                .validate("//pfx:HelloResponse/pfx:CorrelationId", "${correlationId}")
                .validate("//pfx:HelloResponse/pfx:Text", "citrus:concat('Hello ', ${user})")
                .namespace("pfx", "http://www.consol.de/schemas/samples/sayHello.xsd")
                .header("Operation", "sayHello")
                .header("CorrelationId", "${correlationId}")
        ).exception(ValidationException.class);
        
        echo("Test: Failure because of XML xpath validation");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        assertException(
            receive("helloResponseReceiver")
                .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                               "<MessageId>${messageId}</MessageId>" +
                               "<CorrelationId>${correlationId}</CorrelationId>" +
                               "<User>HelloService</User>" +
                               "<Text>Hello ${user}</Text>" +
                           "</HelloResponse>")
                .validateScript("assert root.Text == 'Hello ${user}'")
                .validate("//pfx:HelloResponse/pfx:MessageId", "${messageId}")
                .validate("//pfx:HelloResponse/pfx:CorrelationId", "${correlationId}")
                .validate("//pfx:HelloResponse/pfx:Text", "Something else")
                .namespace("pfx", "http://www.consol.de/schemas/samples/sayHello.xsd")
                .header("Operation", "sayHello")
                .header("CorrelationId", "${correlationId}")
        ).exception(ValidationException.class);
    }
}