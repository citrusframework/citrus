package com.consol.citrus.functions;

import com.consol.citrus.functions.core.AbsoluteFunction;
import com.consol.citrus.functions.core.AvgFunction;
import com.consol.citrus.functions.core.CeilingFunction;
import com.consol.citrus.functions.core.ChangeDateFunction;
import com.consol.citrus.functions.core.ConcatFunction;
import com.consol.citrus.functions.core.CurrentDateFunction;
import com.consol.citrus.functions.core.DecodeBase64Function;
import com.consol.citrus.functions.core.DigestAuthHeaderFunction;
import com.consol.citrus.functions.core.EncodeBase64Function;
import com.consol.citrus.functions.core.FloorFunction;
import com.consol.citrus.functions.core.LoadMessageFunction;
import com.consol.citrus.functions.core.LocalHostAddressFunction;
import com.consol.citrus.functions.core.LowerCaseFunction;
import com.consol.citrus.functions.core.MaxFunction;
import com.consol.citrus.functions.core.MinFunction;
import com.consol.citrus.functions.core.RandomEnumValueFunction;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.functions.core.RandomStringFunction;
import com.consol.citrus.functions.core.RandomUUIDFunction;
import com.consol.citrus.functions.core.ReadFileResourceFunction;
import com.consol.citrus.functions.core.RoundFunction;
import com.consol.citrus.functions.core.StringLengthFunction;
import com.consol.citrus.functions.core.SubstringAfterFunction;
import com.consol.citrus.functions.core.SubstringBeforeFunction;
import com.consol.citrus.functions.core.SubstringFunction;
import com.consol.citrus.functions.core.SumFunction;
import com.consol.citrus.functions.core.SystemPropertyFunction;
import com.consol.citrus.functions.core.TranslateFunction;
import com.consol.citrus.functions.core.UpperCaseFunction;
import com.consol.citrus.functions.core.UrlDecodeFunction;
import com.consol.citrus.functions.core.UrlEncodeFunction;
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
