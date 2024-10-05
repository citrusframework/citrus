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

package org.citrusframework.endpoint;

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointProperty;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AbstractEndpointBuilderTest  {


    @CitrusEndpoint(
        name = "fooEndpoint",
        properties = {
            @CitrusEndpointProperty(name = "message", value = "Hello from Citrus!"),
            @CitrusEndpointProperty(name = "number", value = "1", type = int.class)

        }
    )
    private Endpoint injected;
//
  //  private TestEndpointBuilder endpointBuilder = new TestEndpointBuilder();

    @Test
    public void buildFromEndpointProperties() {

      //  Assert.assertEquals(1,1);
    }

    public static final class TestEndpointBuilder extends AbstractEndpointBuilder<Endpoint> {


       Endpoint mockEndpoint = Mockito.mock(Endpoint.class);

        @Override
        protected Endpoint getEndpoint() {
            return mockEndpoint;
        }

    }
}
