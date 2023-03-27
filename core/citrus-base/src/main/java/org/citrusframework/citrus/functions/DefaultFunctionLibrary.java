package org.citrusframework.citrus.functions;

import org.citrusframework.citrus.functions.core.AbsoluteFunction;
import org.citrusframework.citrus.functions.core.AvgFunction;
import org.citrusframework.citrus.functions.core.CeilingFunction;
import org.citrusframework.citrus.functions.core.ChangeDateFunction;
import org.citrusframework.citrus.functions.core.ConcatFunction;
import org.citrusframework.citrus.functions.core.CurrentDateFunction;
import org.citrusframework.citrus.functions.core.DecodeBase64Function;
import org.citrusframework.citrus.functions.core.DigestAuthHeaderFunction;
import org.citrusframework.citrus.functions.core.EncodeBase64Function;
import org.citrusframework.citrus.functions.core.FloorFunction;
import org.citrusframework.citrus.functions.core.LoadMessageFunction;
import org.citrusframework.citrus.functions.core.LocalHostAddressFunction;
import org.citrusframework.citrus.functions.core.LowerCaseFunction;
import org.citrusframework.citrus.functions.core.MaxFunction;
import org.citrusframework.citrus.functions.core.MinFunction;
import org.citrusframework.citrus.functions.core.RandomEnumValueFunction;
import org.citrusframework.citrus.functions.core.RandomNumberFunction;
import org.citrusframework.citrus.functions.core.RandomStringFunction;
import org.citrusframework.citrus.functions.core.RandomUUIDFunction;
import org.citrusframework.citrus.functions.core.ReadFileResourceFunction;
import org.citrusframework.citrus.functions.core.RoundFunction;
import org.citrusframework.citrus.functions.core.StringLengthFunction;
import org.citrusframework.citrus.functions.core.SubstringAfterFunction;
import org.citrusframework.citrus.functions.core.SubstringBeforeFunction;
import org.citrusframework.citrus.functions.core.SubstringFunction;
import org.citrusframework.citrus.functions.core.SumFunction;
import org.citrusframework.citrus.functions.core.SystemPropertyFunction;
import org.citrusframework.citrus.functions.core.TranslateFunction;
import org.citrusframework.citrus.functions.core.UpperCaseFunction;
import org.citrusframework.citrus.functions.core.UrlDecodeFunction;
import org.citrusframework.citrus.functions.core.UrlEncodeFunction;
import org.citrusframework.citrus.functions.core.UnixTimestampFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public class DefaultFunctionLibrary extends FunctionLibrary {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFunctionLibrary.class);

    /**
     * Default constructor adding default function implementations.
     */
    public DefaultFunctionLibrary() {
        setName("citrusFunctionLibrary");

        getMembers().put("randomNumber", new RandomNumberFunction());
        getMembers().put("randomString", new RandomStringFunction());
        getMembers().put("concat", new ConcatFunction());
        getMembers().put("currentDate", new CurrentDateFunction());
        getMembers().put("substring", new SubstringFunction());
        getMembers().put("stringLength", new StringLengthFunction());
        getMembers().put("translate", new TranslateFunction());
        getMembers().put("substringBefore", new SubstringBeforeFunction());
        getMembers().put("substringAfter", new SubstringAfterFunction());
        getMembers().put("round", new RoundFunction());
        getMembers().put("floor", new FloorFunction());
        getMembers().put("ceiling", new CeilingFunction());
        getMembers().put("upperCase", new UpperCaseFunction());
        getMembers().put("lowerCase", new LowerCaseFunction());
        getMembers().put("average", new AvgFunction());
        getMembers().put("minimum", new MinFunction());
        getMembers().put("maximum", new MaxFunction());
        getMembers().put("sum", new SumFunction());
        getMembers().put("absolute", new AbsoluteFunction());
        getMembers().put("randomEnumValue", new RandomEnumValueFunction());
        getMembers().put("randomUUID", new RandomUUIDFunction());
        getMembers().put("encodeBase64", new EncodeBase64Function());
        getMembers().put("decodeBase64", new DecodeBase64Function());
        getMembers().put("urlEncode", new UrlEncodeFunction());
        getMembers().put("urlDecode", new UrlDecodeFunction());
        getMembers().put("digestAuthHeader", new DigestAuthHeaderFunction());
        getMembers().put("localHostAddress", new LocalHostAddressFunction());
        getMembers().put("changeDate", new ChangeDateFunction());
        getMembers().put("readFile", new ReadFileResourceFunction());
        getMembers().put("message", new LoadMessageFunction());
        getMembers().put("systemProperty", new SystemPropertyFunction());
        getMembers().put("unixTimestamp", new UnixTimestampFunction());

        lookupFunctions();
    }

    /**
     * Add custom function implementations loaded from resource path lookup.
     */
    private void lookupFunctions() {
        Function.lookup().forEach((k, m) -> {
            getMembers().put(k, m);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Register function '%s' as %s", k, m.getClass()));
            }
        });
    }
}
