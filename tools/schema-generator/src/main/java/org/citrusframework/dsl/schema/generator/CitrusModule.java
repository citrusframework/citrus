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

package org.citrusframework.dsl.schema.generator;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.MethodScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigPart;
import com.github.victools.jsonschema.generator.SchemaGeneratorGeneralConfigPart;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.TypeContext;
import com.github.victools.jsonschema.generator.TypeScope;
import com.github.victools.jsonschema.generator.impl.module.InlineSchemaModule;
import com.github.victools.jsonschema.generator.naming.CleanSchemaDefinitionNamingStrategy;
import com.github.victools.jsonschema.generator.naming.DefaultSchemaDefinitionNamingStrategy;
import org.citrusframework.dsl.schema.Catalog;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaType;
import org.citrusframework.yaml.SchemaProperty;

/**
 * Custom Json schema generator module that produces the schema based on annotated setter methods.
 * Uses schema property annotations as base for schema property information.
 * Uses virtual getter methods to align with the Json schema generator capabilities.
 */
public class CitrusModule implements Module {

    private Predicate<SchemaProperty> ignoreFilter = schema -> false;
    private boolean addMetaData = true;
    private boolean requireOneOfItem = true;

    private final AtomicBoolean blocked = new AtomicBoolean(false);
    private final InlineSchemaModule inlineSchemaModule = new InlineSchemaModule();

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder schemaGeneratorConfigBuilder) {
        applyToConfigPart(schemaGeneratorConfigBuilder.forTypesInGeneral());
        applyToConfigPart(schemaGeneratorConfigBuilder.forFields());
        applyToConfigPart(schemaGeneratorConfigBuilder.forMethods());
    }

    private void applyToConfigPart(SchemaGeneratorGeneralConfigPart configPart) {
        configPart
                .withTypeAttributeOverride(this::resolveTypeAttribute)
                .withDefinitionNamingStrategy(new CleanSchemaDefinitionNamingStrategy(new DefaultSchemaDefinitionNamingStrategy(), it -> it))
                .withCustomDefinitionProvider(this::resolveCustomDefinition);
    }

    private void resolveTypeAttribute(ObjectNode schemaNode, TypeScope scope, SchemaGenerationContext schemaGenerationContext) {
        SchemaType schemaType = scope.getContext().getTypeAnnotationConsideringHierarchy(scope.getType(), SchemaType.class);
        if (schemaType == null) {
            return;
        }

        if (schemaType.oneOf().length > 0) {
            ArrayNode anyOf = schemaNode.withArray(schemaGenerationContext.getKeyword(SchemaKeyword.TAG_ANYOF));
            if (anyOf.isEmpty()) {
                ArrayNode oneOf = anyOf.addObject().withArray(schemaGenerationContext.getKeyword(SchemaKeyword.TAG_ONEOF));
                for (String oneOfItem : schemaType.oneOf()) {
                    ObjectNode itemNode = oneOf.addObject();
                    itemNode.put(schemaGenerationContext.getKeyword(SchemaKeyword.TAG_TYPE), "object");
                    ObjectNode propertyNode = itemNode.withObject(schemaGenerationContext.getKeyword(SchemaKeyword.TAG_PROPERTIES));
                    propertyNode.set(oneOfItem, schemaNode.get(schemaGenerationContext.getKeyword(SchemaKeyword.TAG_PROPERTIES)).get(oneOfItem));

                    // remove all properties from old item node
                    ((ObjectNode) schemaNode.get(schemaGenerationContext.getKeyword(SchemaKeyword.TAG_PROPERTIES))).putObject(oneOfItem);

                    if (requireOneOfItem) {
                        itemNode.withArray(schemaGenerationContext.getKeyword(SchemaKeyword.TAG_REQUIRED)).add(oneOfItem);
                    }
                }
            }
        }
    }

    private CustomDefinition resolveCustomDefinition(ResolvedType resolvedType, SchemaGenerationContext schemaGenerationContext) {
        if (blocked.compareAndSet(true, false)) {
            // avoid circular schema reference
            return null;
        }

        if (resolvedType.toString().startsWith("java.util.Map") ||
                resolvedType.toString().equals("org.citrusframework.yaml.actions.script.ScriptDefinitionType") ||
                resolvedType.toString().endsWith("BrowserBuilder") ||
                resolvedType.toString().endsWith("EndpointBuilder") ||
                resolvedType.toString().endsWith("EndpointsBuilder") ||
                resolvedType.toString().endsWith("ClientBuilder") ||
                resolvedType.toString().endsWith("ServerBuilder")) {
            blocked.set(true);
            return inlineSchemaModule.provideCustomSchemaDefinition(resolvedType, schemaGenerationContext);
        }

        return null;
    }

    private void applyToConfigPart(SchemaGeneratorConfigPart<?> configPart){
        configPart
                .withTitleResolver(this::resolveTitle)
                .withDescriptionResolver(this::resolveDescription)
                .withStringFormatResolver(this::resolveFormat)
                .withDefaultResolver(this::resolveDefault)
                .withRequiredCheck(this::resolveRequired)
                .withIgnoreCheck(this::resolveIgnore)
                .withInstanceAttributeOverride(this::resolveMetadata)
                .withNumberMultipleOfResolver(this::resolveNumberMultipleOf)
                .withNumberInclusiveMinimumResolver(this::resolveMinimum)
                .withNumberInclusiveMaximumResolver(this::resolveMaximum)
                .withNumberExclusiveMinimumResolver(this::resolveMinimumExclusive)
                .withNumberExclusiveMaximumResolver(this::resolveMaximumExclusive)
                .withStringMinLengthResolver(this::resolveMinLength)
                .withStringMaxLengthResolver(this::resolveMaxLength)
                .withStringPatternResolver(this::resolvePattern);
    }

    private String resolveTitle(MemberScope<?, ?> member){
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::title)
                .filter(title -> !title.isEmpty())
                .orElseGet(() -> StringUtils.convertFirstCharToUpperCase(member.getName()));
    }

    private String resolveDescription(MemberScope<?, ?> member){
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::description)
                .filter(description -> !description.isEmpty())
                .orElse(null);
    }

    private String resolveFormat(MemberScope<?, ?> member){
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::format)
                .filter(typeFormat -> !SchemaProperty.TypeFormat.NONE.equals(typeFormat))
                .map(SchemaProperty.TypeFormat::toString)
                .orElse(null);
    }

    private String resolveDefault(MemberScope<?, ?> member){
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::defaultValue)
                .filter(defaultValue -> !defaultValue.isEmpty())
                .orElse(null);
    }

    private boolean resolveRequired(MemberScope<?, ?> member){
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::required)
                .orElse(Boolean.FALSE);
    }

    private boolean resolveIgnore(MemberScope<?, ?> member){
        return this.getSchemaPropertyAnnotation(member)
                .map(schema -> schema.ignore() || ignoreFilter.test(schema))
                .orElse(Boolean.TRUE);
    }

    protected void resolveMetadata(ObjectNode jsonSchemaAttributesNode, MemberScope<?, ?> member, SchemaGenerationContext context) {
        if (addMetaData) {
            this.getSchemaPropertyAnnotation(member)
                    .map(SchemaProperty::metadata)
                    .ifPresent(metaData -> Stream.of(metaData)
                            .filter(data -> !data.key().isEmpty())
                            .filter(data -> !data.value().isEmpty())
                            .forEach(data -> jsonSchemaAttributesNode.put(data.key(), data.value())));

            this.getSchemaPropertyAnnotation(member)
                    .map(SchemaProperty::advanced)
                    .filter(advanced -> advanced)
                    .ifPresent(advanced -> jsonSchemaAttributesNode.put("$comment", "group:advanced"));
        }
    }

    protected BigDecimal resolveNumberMultipleOf(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::multipleOf)
                .filter(multipleOf -> multipleOf != 0)
                .map(BigDecimal::new)
                .orElse(null);
    }

    protected BigDecimal resolveMinimum(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .filter(jsonSchema -> !jsonSchema.exclusiveMin())
                .map(SchemaProperty::min)
                .filter(min -> min != Double.MIN_VALUE)
                .map(BigDecimal::new)
                .orElse(null);
    }

    protected BigDecimal resolveMaximum(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .filter(jsonSchema -> !jsonSchema.exclusiveMax())
                .map(SchemaProperty::max)
                .filter(max -> max != Double.MAX_VALUE)
                .map(BigDecimal::new)
                .orElse(null);
    }


    protected BigDecimal resolveMinimumExclusive(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .filter(SchemaProperty::exclusiveMin)
                .map(SchemaProperty::min)
                .filter(min -> min != Double.MIN_VALUE)
                .map(BigDecimal::new)
                .orElse(null);
    }

    protected BigDecimal resolveMaximumExclusive(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .filter(SchemaProperty::exclusiveMax)
                .map(SchemaProperty::max)
                .filter(max -> max != Double.MAX_VALUE)
                .map(BigDecimal::new)
                .orElse(null);
    }

    protected Integer resolveMinLength(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::minLength)
                .filter(minLength -> minLength > 0)
                .orElse(null);
    }

    protected Integer resolveMaxLength(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::maxLength)
                .filter(maxLength -> maxLength != Integer.MAX_VALUE)
                .orElse(null);
    }

    protected String resolvePattern(MemberScope<?, ?> member) {
        return this.getSchemaPropertyAnnotation(member)
                .map(SchemaProperty::pattern)
                .filter(pattern -> !pattern.isEmpty())
                .orElse(null);
    }

    /**
     * Retrieves the annotation instance of the given type, either from the field itself or (if not present) from its getter.
     */
    protected Optional<SchemaProperty> getSchemaPropertyAnnotation(MemberScope<?, ?> member) {
        return Optional.ofNullable(member.getAnnotation(SchemaProperty.class));
    }

    /**
     * Adds additional filter on the property schema annotation.
     */
    public CitrusModule withIgnoreFilter(Predicate<SchemaProperty> filter) {
        this.ignoreFilter = filter;
        return this;
    }

    /**
     * Enable/disable property metadata (e.g. property group information).
     */
    public CitrusModule withMetaData(boolean addMetaData) {
        this.addMetaData = addMetaData;
        return this;
    }

    /**
     * Enable/disable adding required information on a one of item.
     */
    public CitrusModule withRequireOneOf(boolean require) {
        this.requireOneOfItem = require;
        return this;
    }

    /**
     * Special type context wrapper makes sure to create method scope wrapper implementations.
     */
    protected static class TypeContextWrapper extends TypeContext {

        private Catalog catalog;

        public TypeContextWrapper(AnnotationConfiguration annotationConfig, SchemaGeneratorConfig generatorConfig) {
            super(annotationConfig, generatorConfig);
        }

        public TypeContextWrapper withCatalog(Catalog catalog) {
            this.catalog = catalog;
            return this;
        }

        public Catalog getCatalog() {
            return catalog;
        }

        @Override
        public MethodScope createMethodScope(ResolvedMethod method, MemberScope.DeclarationDetails declarationDetails) {
            return new CitrusModule.MethodScopeWrapper(method, declarationDetails, this);
        }
    }

    /**
     * Special method scope wrapper that converts setter methods used in YAML to virtual getter methods.
     * Uses the first method argument od the setter as a return type value for the virtual getter.
     * Makes sure to emulate getter behavior when providing method arguments count and return type.
     */
    protected static class MethodScopeWrapper extends MethodScope {

        private final String propertyName;
        private final ResolvedType propertyType;
        private final ResolvedType overrideType;
        private final String overrideName;
        private final TypeContextWrapper typeContext;
        private final SchemaProperty schema;

        protected MethodScopeWrapper(ResolvedMethod method, MemberScope.DeclarationDetails declarationDetails, TypeContextWrapper context) {
            this(method, declarationDetails, context, method.getArgumentType(0), resolvePropertyName(method));
        }

        protected MethodScopeWrapper(ResolvedMethod method, MemberScope.DeclarationDetails declarationDetails, TypeContextWrapper context,
                                     ResolvedType propertyType, String propertyName) {
            this(method, declarationDetails, context, propertyType, propertyName, null, null);
        }

        protected MethodScopeWrapper(ResolvedMethod method, MemberScope.DeclarationDetails declarationDetails, TypeContextWrapper context,
                                     ResolvedType propertyType, String propertyName, ResolvedType overrideType, String overrideName) {
            super(method, declarationDetails, null, context);
            this.propertyName = propertyName;
            this.propertyType = propertyType;
            this.overrideType = overrideType;
            this.overrideName = overrideName;
            this.typeContext = context;

            this.schema = method.getAnnotations().asList().stream()
                        .filter(SchemaProperty.class::isInstance)
                        .map(SchemaProperty.class::cast)
                        .findFirst()
                        .orElse(null);

            if (typeContext.getCatalog() != null && schema != null && schema.kind() != SchemaProperty.Kind.PROPERTY) {
                typeContext.getCatalog().add(new Catalog.CatalogItem(propertyName, propertyType.getErasedType(), schema));
            }
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            if (annotationClass == SchemaProperty.class) {
                return (A) schema;
            }

            return super.getAnnotation(annotationClass);
        }

        @Override
        public int getArgumentCount() {
            return 0;
        }

        @Override
        public String getDeclaredName() {
            return "get" + StringUtils.convertFirstCharToUpperCase(propertyName);
        }

        @Override
        public boolean isGetter() {
            return true;
        }

        @Override
        public ResolvedType getTypeParameterFor(Class<?> erasedSuperType, int parameterIndex) {
            return this.typeContext.getTypeParameterFor(getType(), erasedSuperType, parameterIndex);
        }

        @Override
        public MethodScope withOverriddenType(ResolvedType overriddenType) {
            return new MethodScopeWrapper(this.getMember(), this.getDeclarationDetails(), typeContext, propertyType, propertyName, overriddenType, overrideName);
        }

        @Override
        public MethodScope withOverriddenName(String overriddenName) {
            return new MethodScopeWrapper(this.getMember(), this.getDeclarationDetails(), typeContext, propertyType, propertyName, overrideType, overriddenName);
        }

        @Override
        public ResolvedType getDeclaredType() {
            return propertyType;
        }

        @Override
        public ResolvedType getOverriddenType() {
            return overrideType;
        }

        @Override
        public String getOverriddenName() {
            return overrideName;
        }

        @Override
        public ResolvedType getType() {
            return Optional.ofNullable(overrideType).orElse(propertyType);
        }

        @Override
        public String getName() {
            return propertyName;
        }

        @Override
        protected String doGetSchemaPropertyName() {
            return propertyName;
        }

        private static String resolvePropertyName(ResolvedMethod method) {
            if (method.getRawMember().isAnnotationPresent(SchemaProperty.class)) {
                String overwriteName = method.getRawMember().getAnnotation(SchemaProperty.class).name();
                if (!overwriteName.isEmpty()) {
                    return overwriteName;
                }
            }

            if (method.getName().startsWith("set")) {
                return StringUtils.convertFirstCharToLowerCase(method.getName().substring("set".length()));
            }
            return StringUtils.convertFirstCharToLowerCase(method.getName());
        }
    }
}
