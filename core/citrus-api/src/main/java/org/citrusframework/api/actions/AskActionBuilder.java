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

package org.citrusframework.api.actions;

import org.citrusframework.Answer;
import org.citrusframework.Question;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface AskActionBuilder<V, T extends TestAction>
        extends ActionBuilder<T, AskActionBuilder<V, T>>, TestActionBuilder<T> {

    AskActionBuilder<V, T> question(Question<V> question);

    AskActionBuilder<V, T> into(Answer<V> holder);

    AskActionBuilder<V, T> saveAs(String variableName);

    interface BuilderFactory {

        <V> AskActionBuilder<V, ?> ask(Question<V> question);

        <V> AskActionBuilder<V, ?> ask();
    }
}
