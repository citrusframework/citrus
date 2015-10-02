package com.consol.citrus.sample;

import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * This is a sample test
 *
 * @author Christoph
 * @since 2015-10-02
 */
@Test
public class SampleIT extends AbstractTestNGCitrusTest {

    @CitrusXmlTest(name = "SampleIT")
    public void sampleIT() {}
}
