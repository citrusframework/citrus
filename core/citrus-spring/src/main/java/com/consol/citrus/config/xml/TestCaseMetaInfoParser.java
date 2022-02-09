package com.consol.citrus.config.xml;

import com.consol.citrus.TestCaseMetaInfo;


/**
 * Default implementation that provides {@link TestCaseMetaInfo}
 */
public class TestCaseMetaInfoParser extends BaseTestCaseMetaInfoParser<TestCaseMetaInfo> {

    public TestCaseMetaInfoParser() {
        super(TestCaseMetaInfo.class);
    }
}