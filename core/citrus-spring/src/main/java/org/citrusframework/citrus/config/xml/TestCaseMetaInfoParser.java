package org.citrusframework.citrus.config.xml;

import org.citrusframework.citrus.TestCaseMetaInfo;


/**
 * Default implementation that provides {@link TestCaseMetaInfo}
 */
public class TestCaseMetaInfoParser extends BaseTestCaseMetaInfoParser<TestCaseMetaInfo> {

    public TestCaseMetaInfoParser() {
        super(TestCaseMetaInfo.class);
    }
}