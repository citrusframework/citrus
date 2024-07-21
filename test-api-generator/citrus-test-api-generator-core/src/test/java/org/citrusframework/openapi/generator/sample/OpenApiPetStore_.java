package org.citrusframework.openapi.generator.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.citrusframework.builder.WithExpressions;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.openapi.generator.sample.PetApi.FindPetByStatusActionBuilder;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.VariableExtractorAdapter;
import org.springframework.http.HttpStatus;

public class OpenApiPetStore_ {

    public static OpenApiPetStore_ openApiPetStore(HttpClient httpClient) {
        return new OpenApiPetStore_();
    }

    public GetPetIdRequestActionBuilder getPetById() {
        return new GetPetIdRequestActionBuilder();
    }

    public FindPetByStatusActionBuilder findByStatus() {
        return new FindPetByStatusActionBuilder();
    }

    public static class GetPetIdRequestActionBuilder extends HttpClientRequestActionBuilder {

        public GetPetIdRequestActionBuilder withPetId(String petId) {
            return this;
        }

    }

    public GetPetIdResponseActionBuilder receivePetById(HttpStatus status) {
        return new GetPetIdResponseActionBuilder();
    }

    public GetPetIdResponseActionBuilder200 receivePetById200() {
        return new GetPetIdResponseActionBuilder200();
    }

    public static class GetPetIdResponseActionBuilder extends HttpClientResponseActionBuilder {

    }

    // Per configured response
    public static class GetPetIdResponseActionBuilder200 extends HttpClientResponseActionBuilder {

        public HttpMessageBuilderSupport withPet(
            Consumer<PetEntityValidationContext.Builder> validator) {
            PetEntityValidationContext.Builder builder = new PetEntityValidationContext.Builder();
            validator.accept(builder);
            return message().validate(builder);
        }
    }

    public static class EntityValidationContext extends DefaultValidationContext {

        private Map<String, Object> expressions;


        private Map<String, EntityValidationContext> nestedValidationContextsBuilders = new HashMap<>();

        public EntityValidationContext(Builder<?, ?> builder) {
            super();
            this.expressions = builder.expressions;
            builder.nestedValidationContextBuilders.forEach((key, value) ->
                nestedValidationContextsBuilders.put(key, value.build()));

        }

        public Map<String, Object> getJsonPathExpressions() {
            return  expressions;
        }

        public Map<String, EntityValidationContext> getNestedValidationContextsBuilders() {
            return nestedValidationContextsBuilders;
        }

        public static class Builder<T extends EntityValidationContext, B extends Builder<T, B>> implements
            ValidationContext.Builder<EntityValidationContext, B>,
            WithExpressions<B>, VariableExtractorAdapter,
            MessageProcessorAdapter {

            private final Map<String, Object> expressions = new HashMap<>();

            protected final Map<String, Builder<?, ?>> nestedValidationContextBuilders= new HashMap<>();

            @Override
            public B expressions(Map<String, Object> expressions) {
                this.expressions.putAll(expressions);
                return (B) this;
            }

            @Override
            public B expression(final String expression,
                final Object value) {
                this.expressions.put(expression, value);
                return (B) this;
            }

            @Override
            public EntityValidationContext build() {
                return new EntityValidationContext(this);
            }

            @Override
            public MessageProcessor asProcessor() {
                return new DelegatingPathExpressionProcessor.Builder()
                    .expressions(expressions)
                    .build();
            }

            @Override
            public VariableExtractor asExtractor() {
                return new DelegatingPayloadVariableExtractor.Builder()
                    .expressions(expressions)
                    .build();
            }

        }
    }

    public static class PetEntityValidationContext extends EntityValidationContext {

        public PetEntityValidationContext(Builder builder) {
            super(builder);
        }

        public static class Builder extends
            EntityValidationContext.Builder<EntityValidationContext, Builder> {

            public Builder id(String expression) {
                return expression("$.id", expression);
            }

            public Builder name(String expression) {
                return expression("$.name", expression);
            }

            public Builder category(String expression) {
                return expression("$.category", expression);
            }

            public Builder urls(String expression) {
                return expression("$.urls", expression);
            }


            public Builder urls(int index, String expression) {
                return expression("$.urls[%d]".formatted(index), expression);
            }

            public Builder address(Consumer<AddressEntityValidationContext.Builder> validator) {
                AddressEntityValidationContext.Builder addressEntityValidationContextBuilder = new AddressEntityValidationContext.Builder();
                validator.accept(addressEntityValidationContextBuilder);
                nestedValidationContextBuilders.put("$.address", addressEntityValidationContextBuilder);

                return this;
            }


            public Builder owners(AggregateEntityValidationContext.Builder<OwnerEntityValidationContext.Builder> aggregateContext) {
                nestedValidationContextBuilders.put("$.owners", aggregateContext);
                return this;
            }

            public static Builder pet() {
                return new Builder();
            }

            @Override
            public PetEntityValidationContext build() {
                return new PetEntityValidationContext(this);
            }

        }
    }

    public static class AddressEntityValidationContext extends EntityValidationContext {

        public AddressEntityValidationContext(Builder builder) {
            super(builder);
        }

        public static class Builder extends
            EntityValidationContext.Builder<EntityValidationContext, Builder> {


            public Builder street(String expression) {
                return expression("$.street", expression);
            }

            public Builder city(String expression) {
                return expression("$.city", expression);
            }

            public Builder zip(String expression) {
                return expression("$.zip", expression);
            }

            public static Builder address() {
                return new Builder();
            }

            @Override
            public AddressEntityValidationContext build() {
                return new AddressEntityValidationContext(this);
            }

        }
    }

    public static class OwnerEntityValidationContext extends EntityValidationContext {

        public OwnerEntityValidationContext(Builder builder) {
            super(builder);
        }

        public static class Builder extends
            EntityValidationContext.Builder<EntityValidationContext, AddressEntityValidationContext.Builder> {

            public Builder address(Consumer<AddressEntityValidationContext.Builder> validator) {
                AddressEntityValidationContext.Builder addressEntityValidationContextBuilder = new AddressEntityValidationContext.Builder();
                validator.accept(addressEntityValidationContextBuilder);
                nestedValidationContextBuilders.put("address", addressEntityValidationContextBuilder);
                return this;
            }

            public Builder name(String expression) {
                expression("$.name", expression);
                return this;
            }

            @Override
            public OwnerEntityValidationContext build() {
                return new OwnerEntityValidationContext(this);
            }
        }
    }

    public static class AggregateEntityValidationContext<T extends EntityValidationContext.Builder<?,?>> extends EntityValidationContext {

        private final Type type;

        private List<Consumer<T>> validator;

        public AggregateEntityValidationContext(Builder<T> builder) {
            super(builder);

            this.type = builder.type;
            this.validator = builder.validator;
        }

        public enum Type {
            ONE_OF, ANY_OF, ALL_OF, NONE_OF
        }

        public static class Builder<T extends EntityValidationContext.Builder<?,?>> extends  EntityValidationContext.Builder<AggregateEntityValidationContext<T>, Builder<T>> {

            private final Type type;

            private final List<Consumer<T>> validator;

            public Builder(Type type, List<Consumer<T>> validator) {
                this.type = type;
                this.validator = validator;
            }

            public static <T extends EntityValidationContext.Builder<?,?>> Builder<T> anyOf(List<Consumer<T>> validator) {
                return new Builder<>(Type.ANY_OF, validator);
            }

            public static <T extends EntityValidationContext.Builder<?,?>> Builder<T> allOf(List<Consumer<T>> validator) {
                return new Builder<>(Type.ALL_OF, validator);
            }

            public static <T extends EntityValidationContext.Builder<?,?>> Builder<T> noneOf(List<Consumer<T>> validator) {

                return new Builder<>(Type.NONE_OF, validator);
            }

            public static <T extends EntityValidationContext.Builder<?,?>> Builder<T> oneOf(List<Consumer<T>> validator) {
                return new Builder<>(Type.ONE_OF, validator);
            }

            @Override
            public AggregateEntityValidationContext build() {
                return null;
            }
        }

    }
}