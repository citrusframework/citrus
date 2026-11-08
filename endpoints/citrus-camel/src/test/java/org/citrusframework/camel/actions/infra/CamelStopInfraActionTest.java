/*
 * Copyright the original author or authors.
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

package org.citrusframework.camel.actions.infra;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CamelStopInfraActionTest extends org.citrusframework.junit.jupiter.AbstractUnitTest {

    @Test
    public void testStopInfra() {
        InfraService meta = new InfraService(
                MyService.class.getName(),
                MyServiceImpl.class.getName(),
                "Sample service",
                Collections.singletonList("my-service"),
                Collections.emptyList(),
                "org.citrusframework",
                "camel-infra-service",
                "1.0",
                "1.0");
        context.setVariable("citrus.camel.infra.my-service:meta", meta);

        MyServiceImpl myService = new MyServiceImpl();
        context.setVariable("citrus.camel.infra.my-service", myService);
        Assertions.assertFalse(myService.shutdown.get());

        CamelStopInfraAction action = new CamelStopInfraAction.Builder()
                .service("my-service")
                .build();

        action.execute(context);

        Assertions.assertTrue(myService.shutdown.get());
    }

    @Test
    public void testStopInfraWithImplementation() {
        InfraService meta = new InfraService(
                MyService.class.getName(),
                MyServiceImpl.class.getName(),
                "Sample service",
                Collections.singletonList("my-service"),
                Arrays.asList("one", "two", "three"),
                "org.citrusframework",
                "camel-infra-service",
                "1.0",
                "1.0");
        context.setVariable("citrus.camel.infra.my-service.one:meta", meta);

        MyServiceImpl myService = new MyServiceImpl();
        context.setVariable("citrus.camel.infra.my-service.one", myService);
        Assertions.assertFalse(myService.shutdown.get());

        CamelStopInfraAction action = new CamelStopInfraAction.Builder()
                .service("my-service", "one")
                .build();

        action.execute(context);

        Assertions.assertTrue(myService.shutdown.get());
    }

    @Test
    public void testStopInfraNotFound() {

        CitrusRuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(CitrusRuntimeException.class, () -> {

            CamelStopInfraAction action = new CamelStopInfraAction.Builder()
                    .service("unknown")
                    .build();

            action.execute(context);
    
        });

        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().matches("No such Camel infra service 'unknown' in current test context"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void testStopInfraImplementationNotFound() {

        CitrusRuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(CitrusRuntimeException.class, () -> {

            CamelStopInfraAction action = new CamelStopInfraAction.Builder()
                    .service("service", "unknown")
                    .build();

            action.execute(context);
    
        });

        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().matches("No such Camel infra service 'service.unknown' in current test context"), "Unexpected exception message: " + thrown.getMessage());
    }
}
