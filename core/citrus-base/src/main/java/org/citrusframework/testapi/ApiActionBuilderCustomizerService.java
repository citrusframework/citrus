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

package org.citrusframework.testapi;

import org.citrusframework.TestAction;
import org.citrusframework.actions.SendMessageAction.SendMessageActionBuilder;
import org.citrusframework.context.TestContext;

/**
 * Implementors of this interface are used to customize the SendMessageActionBuilder with application specific information. E.g. cookies
 * or transactionIds.
 */
public interface ApiActionBuilderCustomizerService {
    <T extends SendMessageActionBuilder<?,?,?>> T build(GeneratedApi generatedApi, TestAction action, TestContext context, T builder);
}
