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

package org.citrusframework.citrus.dsl.endpoint.designer;

import org.citrusframework.citrus.dsl.design.ExecutableTestDesignerComponent;
import org.citrusframework.citrus.message.MessageType;
import org.springframework.stereotype.Component;

@Component("FooTestDesigner")
public class FooTestDesigner extends ExecutableTestDesignerComponent {

    @Override
    public void configure() {
        receive("inboundChannelEndpoint")
                .messageType(MessageType.PLAINTEXT)
                .payload("<TestDesigner name=\"FooTestDesigner\"></TestDesigner>");

        send("inboundChannelEndpoint")
                .payload("<TestDesigner name=\"FooTestDesigner\">OK</TestDesigner>");

        echo("Foo Test OK!");
    }
}
