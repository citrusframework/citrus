package com.consol.citrus.util;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.VariableExpressionIterator.VariableSegment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This {@link Iterator} uses a regular expression pattern to match individual
 * segments of a variableExpression. Each segment of a variable expression
 * represents a bean, either stored as variable in the TestContext (first
 * segment) or a property of the previous bean (all other segments). The
 * iterator provides VariableSegments which provide the name and an optional
 * index as well as the variable/property value corresponding to the segment.
 * <br><br>
 * Example:<br><br>
 * <b>var1.persons[2].firstnames[0]</b><br>
 * <br>
 * The iterator will provide the following VariableSegments for this expression
 * <ol>
 *   <li>the variable with name <b>var1</b> from the TestContext</li>
 *   <li>the third element of the <b>persons</b> property of the variable retrieved in the previous step</li>
 *   <li>the first element of the <b>firstnames</b> property of the property retrieved in the previous step</li>
 * </ol>

 * 
 * @author Thorsten Schlathoelter
 *
 */
public class VariableExpressionIterator implements Iterator<VariableSegment> {

    /**
     * Pattern to parse a variable expression of type 'var1.var2[1].var3'
     */
    private static final Pattern varPathPattern = Pattern.compile("(([^\\[\\]\\.]+)(\\[([0-9])\\])?)(\\.|$)");

    /**
     * The group index for the name of the property
     */
    private static final int NAME_GROUP = 2;

    /**
     * The group index for the index property when accessing array elements
     */
    private static final int INDEX_GROUP = 4;

    /**
     * The variable expression to iterate
     */
    private String variableExpression;

    /**
     * The matcher used to match the variableExpression
     */
    private Matcher matcher;

    /**
     * The nextSegment that is provided by the Iterator
     */
    private VariableSegment nextSegment;

    /**
     * The TestContext
     */
    private TestContext testContext;

    public VariableExpressionIterator(String variableExpression, TestContext testContext) {
        this.variableExpression = variableExpression;
        this.testContext = testContext;

        matcher = varPathPattern.matcher(variableExpression);

        if (matcher.find()) {
            nextSegment = createSegmentValue(matcher.group(NAME_GROUP), matcher.group(INDEX_GROUP), testContext);
        } else {
            throw new CitrusRuntimeException(
                    String.format("Cannot match a segment on variableExpression:", variableExpression));
        }
    }

    @Override
    public boolean hasNext() {
        return nextSegment != null;
    }

    @Override
    public VariableSegment next() {
        VariableSegment ret = nextSegment;
        if (matcher.find()) {
            nextSegment = createSegmentValue(matcher.group(NAME_GROUP), matcher.group(INDEX_GROUP),
                    nextSegment.getSegmentValue());
        } else {
            nextSegment = null;
        }

        return ret;
    }

    private VariableSegment createSegmentValue(String name, String index, Object parentValue) {

        Object segmentValue = null;
        if (parentValue instanceof TestContext) {
            segmentValue = getValueFromContext(name);
        } else {
            segmentValue = getValueFromObjectField(name, parentValue);
        }

        if (StringUtils.hasLength(index)) {
            segmentValue = getIndexedElement(name, index, segmentValue);
        }

        return new VariableSegment(name, index, segmentValue);
    }

    private Object getValueFromObjectField(String name, Object parentValue) {
        Field field = ReflectionUtils.findField(parentValue.getClass(), name);
        if (field == null) {
            throw new CitrusRuntimeException(String.format("Failed to get variable - unknown field '%s' on type %s",
                    name, parentValue.getClass().getName()));
        }

        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, parentValue);
    }

    private Object getValueFromContext(String name) {
        Object matchedVariableValue = null;
        if (testContext.getVariables().containsKey(name)) {
            matchedVariableValue = testContext.getVariables().get(name);
        } else {
            throw new CitrusRuntimeException("Unknown variable '" + variableExpression + "'");
        }
        return matchedVariableValue;
    }

    private Object getIndexedElement(String name, String index, Object arrayValue) {
        Object indexValue = null;
        if (arrayValue.getClass().isArray()) {
            indexValue = Array.get(arrayValue, Integer.valueOf(index));
        } else {
            throw new CitrusRuntimeException(
                    String.format("Expected an instance of Array type. Cannot retrieve indexed property %s from %s ",
                            name, arrayValue.getClass().getName()));
        }
        return indexValue;
    }

    public static Object getLastExpressionValue(String variableExpression, TestContext testContext) {
        VariableSegment segment = null;
        VariableExpressionIterator iterator = new VariableExpressionIterator(variableExpression, testContext);
        while (iterator.hasNext()) {
            segment = iterator.next();
        }

        return segment.getSegmentValue();
    }

    public static class VariableSegment {

        /**
         * The name of the variableExpression segment
         */
        private final String name;

        /**
         * An optional index if the VariableSegment represents an array
         */
        private final String index;

        /**
         * The evaluated value for this segment
         */
        private final Object segmentValue;

        public VariableSegment(String name, String index, Object segmentValue) {
            super();
            this.name = name;
            this.index = index;
            this.segmentValue = segmentValue;
        }

        public String getName() {
            return name;
        }

        public String getIndex() {
            return index;
        }

        public Object getSegmentValue() {
            return segmentValue;
        }
    }
}
