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

package org.citrusframework.variable;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.SegmentEvaluationException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.citrusframework.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Simple registry holding all available segment variable extractor implementations. Test context can ask this registry for
 * the extractors managed by this registry in order to access variable content from the TestContext expressed by variable expressions.
 * <p>
 * Registry provides all known {@link SegmentVariableExtractor}s.
 */
public class SegmentVariableExtractorRegistry {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SegmentVariableExtractorRegistry.class);

    /** Segment variable extractor resource lookup path */
    private static final String RESOURCE_PATH = "META-INF/citrus/variable/extractor/segment";

    /** Type resolver to find custom segment variable extractors on classpath via resource path lookup */
    private static final TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves extractor from resource path lookup with given extractor resource name. Scans classpath for extractor meta information
     * with given name and returns instance of extractor. Returns optional instead of throwing exception when no extractor
     * could be found.
     */
    static Collection<SegmentVariableExtractor> lookup() {
        try {
            Map<String, SegmentVariableExtractor> extractors = TYPE_RESOLVER.resolveAll();
            return extractors.values();
        } catch (CitrusRuntimeException e) {
            logger.warn("Failed to resolve segment variable extractor from resource '{}'", RESOURCE_PATH);
        }

        return Collections.emptyList();
    }

    /**
     * SegmentVariableExtractors to extract values from value representations of individual segments.
     */
    private final List<SegmentVariableExtractor> segmentValueExtractors = new ArrayList<>(List.of(
        MapVariableExtractor.INSTANCE, ObjectFieldValueExtractor.INSTANCE));

    public SegmentVariableExtractorRegistry() {
        segmentValueExtractors.addAll(lookup());
    }

    /**
     * Obtain the segment variable extractors managed by the registry

     */
    public List<SegmentVariableExtractor> getSegmentValueExtractors() {
        return segmentValueExtractors;
    }

    /**
     * Base class for segment variable extractors that ensures that an exception is thrown upon no match.
     */
    public abstract static class AbstractSegmentVariableExtractor implements SegmentVariableExtractor {

        @Override
        public final Object extractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {

            try {
                return doExtractValue(testContext, object, matcher);
            } catch (SegmentEvaluationException e) {
                throw createMatchFailureException(matcher, object, e);
            }
        }

        /**
         * Builds a {@link CitrusRuntimeException} describing why a variable/segment could not be resolved.
         */
        private static CitrusRuntimeException createMatchFailureException(VariableExpressionSegmentMatcher matcher, Object object, SegmentEvaluationException cause) {

            String expr    = nullSafe(matcher.getVariableExpression());
            String segment = nullSafe(matcher.getSegmentExpression());
            int idx        = safeIndex(matcher.getSegmentIndex());
            int total      = safeIndex(matcher.getTotalSegmentCount());

            String objectType = (object == null) ? "null" : object.getClass().getName();

            StringBuilder sb = new StringBuilder(256)
                .append("Unable to extract value using expression '").append(expr).append("'!");

            if (total > 1 && idx >= 1 && idx <= total) {
                sb.append(" — failed at segment '").append(segment)
                    .append("' (").append(idx).append('/').append(total).append(')');
            }

            if (cause != null) {
                sb.append(format("%nReason: %s.",
                    cause.getMessage() == null ? "" : cause.getMessage()
                ));
            }

            sb.append(format("%nFrom object (%s):%n%s", objectType, cause != null ? cause.getRenderedObject() : ""));

            return new CitrusRuntimeException(sb.toString());
        }

        protected abstract Object doExtractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) throws SegmentEvaluationException;

        private static String nullSafe(String s) { return s == null ? "<null>" : s; }

        private static int safeIndex(int i) { return Math.max(0, i); }
    }



    /** Minimal, safe rendering used as fallback (truncate huge payloads). */
    protected static String renderObjectMinimal(Object object) {
        if (object == null) return "null";
        return String.valueOf(object);
    }

    /**
     * Base class for extractors that support an optional [index] on the segment.
     */
    public abstract static class IndexedSegmentVariableExtractor extends AbstractSegmentVariableExtractor {

        @Override
        public final Object doExtractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher)
            throws SegmentEvaluationException {

            Object extractedValue = doExtractIndexedValue(testContext, object, matcher);

            if (matcher.getSegmentIndex() != -1) {
                extractedValue = getIndexedElement(object, matcher, extractedValue);
            }
            return extractedValue;
        }

        /**
         * Return the element at the given index from arrays or lists. Throw SegmentEvaluationException for errors.
         */
        private Object getIndexedElement(Object root, VariableExpressionSegmentMatcher matcher, Object indexedValue)
            throws SegmentEvaluationException {

            int idx = matcher.getSegmentIndex();

            if (indexedValue == null) {
                throw new SegmentEvaluationException(
                    format("Cannot index into null for segment '%s' (index %d)",
                        matcher.getSegmentExpression(), idx),
                    renderObjectMinimal(root));
            }

            // Java array
            if (indexedValue.getClass().isArray()) {
                int length = Array.getLength(indexedValue);
                if (idx < 0 || idx >= length) {
                    throw new SegmentEvaluationException(
                        format("Index %d out of bounds (array length %d) for segment '%s'",
                            idx, length, matcher.getSegmentExpression()),
                        renderObjectMinimal(root));
                }
                return Array.get(indexedValue, idx);
            }

            // java.util.List
            if (indexedValue instanceof List<?> list) {
                int length = list.size();
                if (idx < 0 || idx >= length) {
                    throw new SegmentEvaluationException(
                        format("Index %d out of bounds (list size %d) for segment '%s'",
                            idx, length, matcher.getSegmentExpression()),
                        renderObjectMinimal(root));
                }
                return list.get(idx);
            }

            // Unsupported type
            throw new SegmentEvaluationException(
                format("Expected array or List for indexed access, but was %s (segment '%s')",
                    indexedValue.getClass().getName(), matcher.getSegmentExpression()),
                renderObjectMinimal(root));
        }

        /** Implement in subclasses: extract the (possibly indexed) container value to index into. */
        protected abstract Object doExtractIndexedValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher)
            throws SegmentEvaluationException;
    }

    /**
     * Extracts a segment via a declared field on the parent object.
     */
    public static class ObjectFieldValueExtractor extends IndexedSegmentVariableExtractor {

        public static final ObjectFieldValueExtractor INSTANCE = new ObjectFieldValueExtractor();
        private ObjectFieldValueExtractor() {}

        @Override
        protected Object doExtractIndexedValue(TestContext testContext, Object parentObject, VariableExpressionSegmentMatcher matcher)
            throws SegmentEvaluationException {
            try {
                Field field = ReflectionHelper.findField(parentObject.getClass(), matcher.getSegmentExpression());
                if (field == null) {
                    throw new SegmentEvaluationException(
                        format("Unknown field '%s' on type %s",
                            matcher.getSegmentExpression(), parentObject.getClass().getName()),
                        renderObjectMinimal(parentObject));
                }
                return ReflectionHelper.getField(field, parentObject);
            } catch (SegmentEvaluationException see) {
                throw see; // rethrow as-is
            } catch (Exception ex) {
                throw new SegmentEvaluationException(
                    format("Failed to access field '%s' on type %s: %s",
                        matcher.getSegmentExpression(), parentObject.getClass().getName(), ex.getMessage()),
                    renderObjectMinimal(parentObject));
            }
        }

        @Override
        public boolean canExtract(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
            // Objects except Strings (JSON/XML strings handled by dedicated extractors)
            return object != null && !(object instanceof String);
        }
    }

    /**
     * Extracts a segment via Map lookup using the segment expression as key.
     */
    public static class MapVariableExtractor extends IndexedSegmentVariableExtractor {

        public static final MapVariableExtractor INSTANCE = new MapVariableExtractor();
        private MapVariableExtractor() {}

        @Override
        protected Object doExtractIndexedValue(TestContext testContext, Object parentObject, VariableExpressionSegmentMatcher matcher)
            throws SegmentEvaluationException {

            if (!(parentObject instanceof Map<?, ?> map)) {
                throw new SegmentEvaluationException(
                    format("Expected Map for segment '%s' but was %s",
                        matcher.getSegmentExpression(), parentObject == null ? "null" : parentObject.getClass().getName()),
                    renderObjectMinimal(parentObject));
            }

            String key = matcher.getSegmentExpression();
            if (!map.containsKey(key)) {
                throw new SegmentEvaluationException(
                    format("Unknown key '%s' in Map", key),
                    renderObjectMinimal(parentObject));
            }

            // Value may legitimately be null—return it as-is.
            return map.get(key);
        }

        @Override
        public boolean canExtract(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
            return object instanceof Map;
        }
    }

}
