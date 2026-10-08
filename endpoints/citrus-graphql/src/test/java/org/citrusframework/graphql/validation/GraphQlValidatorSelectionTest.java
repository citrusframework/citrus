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
package org.citrusframework.graphql.validation;

import java.util.List;

import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.json.JsonTextMessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlValidatorSelectionTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldSupportOnlyMessagesReceivedThroughGraphQlActions() {
        GraphQlMessageValidator validator = new GraphQlMessageValidator();
        Message plain = new DefaultMessage("{\"data\":{}}");
        Message graphQl = new DefaultMessage("{\"data\":{}}").setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_RESPONSE);

        assertThat(validator.supportsMessageType("JSON", plain)).isFalse();
        assertThat(validator.supportsMessageType("JSON", graphQl)).isTrue();
        assertThat(validator.supportsMessageType("PLAINTEXT", graphQl)).isTrue();
    }

    @Test
    public void shouldNotChangeValidatorsSelectedForPlainJsonMessages() {
        Message plain = new DefaultMessage("{\"data\":{}}");
        Message graphQl = new DefaultMessage("{\"data\":{}}").setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_RESPONSE);

        List<MessageValidator<? extends ValidationContext>> forPlain =
                context.getMessageValidatorRegistry().findMessageValidators("JSON", plain);
        List<MessageValidator<? extends ValidationContext>> forGraphQl =
                context.getMessageValidatorRegistry().findMessageValidators("JSON", graphQl);

        assertThat(forPlain).noneMatch(GraphQlMessageValidator.class::isInstance);
        assertThat(forGraphQl).anyMatch(GraphQlMessageValidator.class::isInstance);
        assertThat(forGraphQl).hasSize(forPlain.size() + 1);
    }

    @Test
    public void shouldNotBeClaimedByJsonValidator() {
        GraphQlMessageValidationContext graphQlContext = GraphQlMessageValidationContext.Builder.response().build();

        assertThat(new JsonTextMessageValidator().findValidationContext(List.of(graphQlContext))).isNull();
    }

    @Test
    public void shouldRequireValidatorAndNameModule() {
        GraphQlMessageValidationContext graphQlContext = GraphQlMessageValidationContext.Builder.response().build();

        assertThat(graphQlContext.requiresValidator()).isTrue();
        assertThat(graphQlContext.getCorrespondingValidationModule()).contains("org.citrusframework:citrus-graphql");
    }

    @Test
    public void shouldLookupValidationContextBuilder() {
        assertThat(ValidationContext.lookup("graphql")).containsInstanceOf(GraphQlMessageValidationContext.Builder.class);
    }
}
