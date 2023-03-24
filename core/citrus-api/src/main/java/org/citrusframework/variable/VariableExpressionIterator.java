package org.citrusframework.variable;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.VariableExpressionIterator.VariableSegment;

import java.util.*;

/**
 * This {@link Iterator} uses a regular expression pattern to match individual
 * segments of a variableExpression. Each segment of a variable expression
 * represents a bean, either stored as variable in the TestContext (first
 * segment) or a property of the previous bean (all other segments). The
 * iterator provides VariableSegments which provide the name and an optional
 * index as well as the variable/property value corresponding to the segment.
 * <p>
 * Example:
 * <b>var1.persons[2].firstnames[0]</b>
 * <p>
 * The iterator will provide the following VariableSegments for this expression
 * <ol>
 *   <li>the variable with name <b>var1</b> from the TestContext</li>
 *   <li>the third element of the <b>persons</b> property of the variable retrieved in the previous step</li>
 *   <li>the first element of the <b>firstnames</b> property of the property retrieved in the previous step</li>
 * </ol>
 *
 * @author Thorsten Schlathoelter
 */
public class VariableExpressionIterator implements Iterator<VariableSegment> {

    /**
     * The matcher used to match the variableExpression
     */
    private final VariableExpressionSegmentMatcher matcher;

    /**
     * The TestContext
     */
    private final TestContext testContext;

    /**
     * The SegmentVariableExtractor
     */
    private final List<SegmentVariableExtractor> segmentValueExtractors;

    /**
     * The nextSegment that is provided by the Iterator. The nextSegment value is always looked ahead to be able to
     * support hasNext.
     */
    private VariableSegment nextSegment;

    public VariableExpressionIterator(String variableExpression, TestContext testContext,
                                      List<SegmentVariableExtractor> segmentValueExtractors) {
        this.testContext = testContext;
        this.segmentValueExtractors = segmentValueExtractors;

        matcher = new VariableExpressionSegmentMatcher(variableExpression);

        if (matcher.nextMatch()) {
            nextSegment = createSegmentValue(testContext.getVariables());
        } else {
            throw new CitrusRuntimeException(String.format("Cannot match a segment on variableExpression: %s",
                    variableExpression));
        }
    }

    /**
     * Returns true if the iterator has a next
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return nextSegment != null;
    }

    /**
     * Returns the next value and looks ahead for yet another next value.
     *
     * @return
     */
    @Override
    public VariableSegment next() {
        VariableSegment currentSegment = nextSegment;

        // Look ahead next segment
        if (matcher.nextMatch()) {
            nextSegment = createSegmentValue(currentSegment.getSegmentValue());
        } else {
            nextSegment = null;
        }

        return currentSegment;
    }

    /**
     * Create the segment value from the current match
     *
     * @param parentValue
     * @return
     */
    private VariableSegment createSegmentValue(Object parentValue) {
        Object segmentValue = segmentValueExtractors.stream().filter(extractor -> extractor.canExtract(testContext,
                parentValue, matcher)).findFirst().map(extractor -> extractor.extractValue(testContext, parentValue,
                matcher)).orElse(null);
        return new VariableSegment(matcher.getSegmentExpression(), matcher.getSegmentIndex(), segmentValue);
    }

    public static Object getLastExpressionValue(String variableExpression, TestContext testContext,
                                                List<SegmentVariableExtractor> extractors) {
        VariableSegment segment = null;
        VariableExpressionIterator iterator = new VariableExpressionIterator(variableExpression, testContext,
                extractors);
        while (iterator.hasNext()) {
            segment = iterator.next();
        }

        return segment != null ? segment.getSegmentValue() : null;
    }

    public static class VariableSegment {

        /**
         * The name of the variableExpression segment
         */
        private final String name;

        /**
         * An optional index if the VariableSegment represents an array. A value of -1 indicates "no index".
         */
        private final int index;

        /**
         * The evaluated value for this segment
         */
        private final Object segmentValue;

        public VariableSegment(String name, int index, Object segmentValue) {
            this.name = name;
            this.index = index;
            this.segmentValue = segmentValue;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public Object getSegmentValue() {
            return segmentValue;
        }
    }
}
