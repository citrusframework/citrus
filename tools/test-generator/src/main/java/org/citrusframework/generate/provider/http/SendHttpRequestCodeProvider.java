/*
 * Copyright 2006-2019 the original author or authors.
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

package org.citrusframework.generate.provider.http;

import org.citrusframework.generate.provider.CodeProvider;
import org.citrusframework.http.message.HttpMessage;
import com.squareup.javapoet.CodeBlock;

/**
 * @since 2.7.4
 */
public class SendHttpRequestCodeProvider implements CodeProvider<HttpMessage>{

    private final HttpCodeProvider httpCodeProvider = new HttpCodeProvider();

    @Override
    public CodeBlock getCode(final String endpoint, final HttpMessage message) {
        final CodeBlock.Builder code = CodeBlock.builder();

        code.add("runner.run(http().client($S)\n", endpoint);
        code.indent();
        code.add(".send()\n");
        httpCodeProvider.provideRequestConfiguration(code, message);
        code.unindent();
        code.add(");");

        return code.build();
    }
}
