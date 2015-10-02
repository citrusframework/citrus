package com.consol.citrus;

import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * TODO: Description
 *
 * @author Unknown
 * @since 2015-10-02
 */
@Test
public class SampleIT extends AbstractTestNGCitrusTest {

    @CitrusXmlTest(name = "SampleIT")
    public void sampleIT() {}
}
