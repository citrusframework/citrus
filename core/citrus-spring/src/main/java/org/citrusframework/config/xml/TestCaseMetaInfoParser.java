package org.citrusframework.config.xml;

import org.citrusframework.TestCaseMetaInfo;


/**
 * Default implementation that provides {@link TestCaseMetaInfo}
 */
public class TestCaseMetaInfoParser extends BaseTestCaseMetaInfoParser<TestCaseMetaInfo> {

    public TestCaseMetaInfoParser() {
        super(TestCaseMetaInfo.class);
    }
}