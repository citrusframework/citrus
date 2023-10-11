package org.citrusframework.variable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.citrusframework.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple registry holding all available segment variable extractor implementations. Test context can ask this registry for
 * the extractors managed by this registry in order to access variable content from the TestContext expressed by variable expressions.
 * <p>
 * Registry provides all known {@link SegmentVariableExtractor}s.
 *
 * @author Thorsten Schlathoelter
 */
public class SegmentVariableExtractorRegistry {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SegmentVariableExtractor.class);

    /** Segment variable extractor resource lookup path */
    private static final String RESOURCE_PATH = "META-INF/citrus/variable/extractor/segment";

    /** Type resolver to find custom segment variable extractors on classpath via resource path lookup */
    private static final TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves extractor from resource path lookup with given extractor resource name. Scans classpath for extractor meta information
     * with given name and returns instance of extractor. Returns optional instead of throwing exception when no extractor
     * could be found.
     * @return
     */
    static Collection<SegmentVariableExtractor> lookup() {
        try {
            Map<String, SegmentVariableExtractor> extractors = TYPE_RESOLVER.resolveAll();
            return extractors.values();
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve segment variable extractor from resource '%s'", RESOURCE_PATH));
        }

        return Collections.emptyList();
    }

    /**
     * SegmentVariableExtractors to extract values from value representations of individual segments.
     */
    private final List<SegmentVariableExtractor> segmentValueExtractors = new ArrayList<>(List.of(MapVariableExtractor.INSTANCE, ObjectFieldValueExtractor.INSTANCE));

    public SegmentVariableExtractorRegistry() {
        segmentValueExtractors.addAll(lookup());
    }

    /**
     * Obtain the segment variable extractors managed by the registry
     *
     * @return
     */
    public List<SegmentVariableExtractor> getSegmentValueExtractors() {
        return segmentValueExtractors;
    }

    /**
     * Base class for segment variable extractors that ensures that an exception is thrown upon no match.
     */
    public static abstract class AbstractSegmentVariableExtractor implements SegmentVariableExtractor {

        @Override
        public final Object extractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
            Object matchedValue = doExtractValue(testContext, object, matcher);

            if (matchedValue == null) {
                handleMatchFailure(matcher);
            }

            return matchedValue;
        }

        /**
         * Handles a match failure by throwing a CitrusException with an appropriate message
         * @param matcher
         */
        private void handleMatchFailure(VariableExpressionSegmentMatcher matcher) {
            String exceptionMessage;
            if (matcher.getTotalSegmentCount() == 1) {
                exceptionMessage = String.format("Unknown variable '%s'" ,
                        matcher.getVariableExpression());
            } else {
                if (matcher.getSegmentIndex() == 1) {
                    exceptionMessage = String.format("Unknown variable for first segment '%s' " +
                                    "of variable expression '%s'",
                            matcher.getSegmentExpression(), matcher.getVariableExpression());
                } else {
                    exceptionMessage = String.format("Unknown segment-value for segment '%s' " +
                                    "of variable expression '%s'",
                            matcher.getSegmentExpression(), matcher.getVariableExpression());
                }
            }
            throw new CitrusRuntimeException(exceptionMessage);
        }

        protected abstract Object doExtractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher);
    }

    /**
     * Base class for extractors that can operate on indexed values.
     */
    public static abstract class IndexedSegmentVariableExtractor extends AbstractSegmentVariableExtractor {

        public final Object doExtractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {

            Object extractedValue = doExtractIndexedValue(testContext, object, matcher);

            if (matcher.getSegmentIndex() != -1) {
                extractedValue = getIndexedElement(matcher, extractedValue);
            }
            return extractedValue;
        }

        /**
         * Get the index element from an indexed value.
         *
         * @param matcher
         * @param indexedValue
         * @return
         */
        private Object getIndexedElement(VariableExpressionSegmentMatcher matcher, Object indexedValue) {
            if (indexedValue.getClass().isArray()) {
                return  Array.get(indexedValue, matcher.getSegmentIndex());
            } else {
                throw new CitrusRuntimeException(
                        String.format("Expected an instance of Array type. Cannot retrieve indexed property %s from %s ",
                                matcher.getSegmentExpression(), indexedValue.getClass().getName()));
            }
        }

        /**
         * Extract the indexed value from the object
         *
         * @param object
         * @param matcher
         * @return
         */
        protected abstract Object doExtractIndexedValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher);
    }

    /**
     * SegmentVariableExtractor that accesses the segment value by a {@link Field} of the parentObject
     */
    public static class ObjectFieldValueExtractor extends IndexedSegmentVariableExtractor {

        public static ObjectFieldValueExtractor INSTANCE = new ObjectFieldValueExtractor();

        private ObjectFieldValueExtractor() {
            // singleton
        }

        @Override
        protected Object doExtractIndexedValue(TestContext testContext, Object parentObject, VariableExpressionSegmentMatcher matcher) {
            Field field = ReflectionHelper.findField(parentObject.getClass(), matcher.getSegmentExpression());
            if (field == null) {
                throw new CitrusRuntimeException(String.format("Failed to get variable - unknown field '%s' on type %s",
                        matcher.getSegmentExpression(), parentObject.getClass().getName()));
            }

            return ReflectionHelper.getField(field, parentObject);
        }

        @Override
        public boolean canExtract(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
            return object != null && !(object instanceof String);
        }
    }

    /**
     * SegmentVariableExtractor that accesses the segment value from a {@link Map}. The extractor uses the segment expression
     * as key into the map.
     */
    public static class MapVariableExtractor extends IndexedSegmentVariableExtractor {

        public static MapVariableExtractor INSTANCE = new MapVariableExtractor();

        private MapVariableExtractor() {
            // singleton
        }

        @Override
        protected Object doExtractIndexedValue(TestContext testContext, Object parentObject, VariableExpressionSegmentMatcher matcher) {

            Object matchedValue = null;
            if (parentObject instanceof Map<?, ?>) {
                matchedValue = ((Map<?, ?>) parentObject).get(matcher.getSegmentExpression());
            }
            return matchedValue;
        }

        @Override
        public boolean canExtract(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
            return object instanceof Map;
        }
    }
}
