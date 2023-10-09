package org.citrusframework.variable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher that matches segments of variable expressions. The matcher is capable to match the following segments:<br>
 * <br>
 * <ul>
 *     <li>indexed variables/properties segments of the form: 'var[1]'</li>
 *     <li>jsonPath segments of the form: 'jsonPath($.person.name)'</li>
 *     <li>xpath segments of the form: 'xpath(//person/name)'</li>
 * </ul>
 * <br>
 * <p>
 * Note that jsonPath and xpath segments must terminate the expression, i.e. they cannot be followed by further expressions.
 * If a variable expression is used to access a variable from the test context it must start with a variable segment which
 * extracts the first variable from the test context.<p>
 * <p>
 * Sample for valid variable expressions:
 * <br>
 *  <ul>
 *  <li>var1</li>
 *       <li>var1.var2</li>
 *       <li>var1[1]</li>
 *       <li>var1[1].var2[2]</li>
 *       <li>var1[1].var2[2].var3</li>
 *       <li>var1.jsonPath($.person.name)</li>
 *       <li>var1[1].jsonPath($.person.name)</li>
 *       <li>var1.xpath(//title[@lang='en'])</li>
 *       <li>var1[1].xpath(//title[@lang='en'])</li>
 *   </ul>
 *   <br>
 */
public class VariableExpressionSegmentMatcher {

    /**
     * Pattern to parse a variable expression
     */
    private static final Pattern VAR_PATH_PATTERN = Pattern.compile("(xpath\\((.*)\\)$)|(jsonPath\\((\\$[.\\[].*)\\)$)|(([^\\[\\].]+)(\\[([0-9])])?)(\\.|$)");

    /**
     * The regex group index for the full xpath segment
     */
    private static final int XPATH_SEGMENT_GROUP = 1;

    /**
     * The regex group index for the xpath path
     */
    private static final int XPATH_GROUP = 2;

    /**
     * The regex group index for the full jsonPath segment
     */
    private static final int JSONPATH_SEGMENT_GROUP = 3;

    /**
     * The regex group index for the jsonPath part
     */
    private static final int JSON_PATH_GROUP = 4;

    /**
     * The regex group index for the full variable/property segment incl. index
     */
    private static final int VAR_PROP_SEGMENT_GROUP = 5;

    /**
     * The regex group index for the name of the variable/property
     */
    private static final int VAR_PROP_NAME_GROUP = 6;

    /**
     * The regex group index for the full name/index expression
     */
    private static final int NAME_INDEX_GROUP = 7;

    /**
     * The regex group index for the index when accessing array elements
     */
    private static final int INDEX_GROUP = 8;

    /**
     * The variable expression the matcher is working on
     */
    private final String variableExpression;

    /**
     * The matcher that performs the actual matching
     */
    private final Matcher matcher;

    /**
     * The total number of segments in the variableExpression
     */
    private final int totalSegmentCount;

    /**
     * The current expression the matcher has matched
     */
    private String currentSegmentExpression;

    /**
     * The current segment expression index. A value of -1 indicates "no index".
     */
    private int currentSegmentIndex = -1;

    public VariableExpressionSegmentMatcher(String variableExpression) {
        this.variableExpression = variableExpression;
        totalSegmentCount =  totalSegmentCount(variableExpression);
        matcher = VAR_PATH_PATTERN.matcher(variableExpression);
    }

    /**
     * Determine the total number of segments in the variable expression
     * @param variableExpression
     * @return
     */
    private int totalSegmentCount(String variableExpression) {
        Matcher matcher = VAR_PATH_PATTERN.matcher(variableExpression);
        return (int)matcher.results().count();
    }

    /**
     *  Obtain the total number of segments in the variable expression of this matcher
     * @return
     */
    public int getTotalSegmentCount() {
        return totalSegmentCount;
    }

    /**
     * Attempts to find the next segment in the variable expression and sets the current
     * segment expression as well as the current segment index.
     *
     * @return
     */
    public boolean nextMatch() {
        boolean matches = matcher.find();

        currentSegmentExpression = null;
        currentSegmentIndex = -1;

        if (matches) {
            if (!Optional.ofNullable(matcher.group(JSON_PATH_GROUP)).orElse("").isEmpty()) {
                currentSegmentExpression = matcher.group(JSON_PATH_GROUP);
            } else if (!Optional.ofNullable(matcher.group(XPATH_GROUP)).orElse("").isEmpty()) {
                currentSegmentExpression = matcher.group(XPATH_GROUP);
            } else {
                currentSegmentExpression = matcher.group(VAR_PROP_NAME_GROUP);
                currentSegmentIndex = matcher.group(INDEX_GROUP) != null ? Integer.parseInt(matcher.group(INDEX_GROUP)) : -1;
           }
        }
        return matches;
    }

    /**
     * Obtain the variable expression which backs the matcher.
     * @return
     */
    public String getVariableExpression() {
        return variableExpression;
    }

    /**
     * Obtain the segment expression ot the current match. Null if the matcher has run out of matches.
     * @return
     */
    public String getSegmentExpression() {
        return currentSegmentExpression;
    }

    /**
     * Obtain the segment index of the current match. -1 if match is not indexed of matcher has run out of matches.
     * @return
     */
    public int getSegmentIndex() {
        return currentSegmentIndex;
    }
}
