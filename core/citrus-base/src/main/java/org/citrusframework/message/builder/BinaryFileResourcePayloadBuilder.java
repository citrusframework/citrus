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

package org.citrusframework.message.builder;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;

public class BinaryFileResourcePayloadBuilder extends FileResourcePayloadBuilder {

    /**
     * Constructor using the script file resource and default charset.
     * @param resourcePath
     */
    public BinaryFileResourcePayloadBuilder(String resourcePath) {
        super(resourcePath);
    }

    /**
     * Constructor using the script file resource and charset.
     * @param resourcePath
     * @param charset
     */
    public BinaryFileResourcePayloadBuilder(String resourcePath, String charset) {
        super(resourcePath, charset);
    }

    /**
     * Constructor using script file resource.
     * @param resource
     */
    public BinaryFileResourcePayloadBuilder(Resource resource) {
        super(resource);
    }

    /**
     * Constructor using script file resource path and default charset.
     * @param resource
     * @param charset
     */
    public BinaryFileResourcePayloadBuilder(Resource resource, String charset) {
        super(resource, charset);
    }

    @Override
    public Object buildPayload(TestContext context) {
        setMessageType(MessageType.BINARY.name());
        return super.buildPayload(context);
    }
}
