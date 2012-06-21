/*
 * File: CitrusTestLinkFileHandlerImplTestCase.java
 *
 * Copyright (c) 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * last modified: Friday, May 18, 2012 (18:50) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

import java.io.File;

import java.util.List;

import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.consol.citrus.TestCase;
import com.consol.citrus.testlink.CitrusTestLinkBean;
import com.consol.citrus.testlink.CitrusTestLinkFileBean;
import com.consol.citrus.testlink.utils.FileUtils;

/**
 * JUnit class for testing {@link CitrusTestLinkFileHandlerImpl} class.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public class CitrusTestLinkFileHandlerImplTestCase {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** BEAN_TEST_CASE_ID. */
    private static final Integer BEAN_TEST_CASE_ID = Integer.valueOf(4711);

    /** BEAN_TEST_CASE_INTERNAL_ID. */
    private static final Integer BEAN_TEST_CASE_INTERNAL_ID = Integer.valueOf(4712);

    /** BEAN_TEST_PLAN_ID. */
    private static final Integer BEAN_TEST_PLAN_ID = Integer.valueOf(4713);

    /** BEAN_BUILD_ID. */
    private static final Integer BEAN_BUILD_ID = Integer.valueOf(4714);

    /** TEST_CASE_NAME. */
    private static final String TEST_CASE_NAME = "Test4711";

    /** BEAN_BUILD_NAME. */
    private static final String BEAN_BUILD_NAME = "testlink.CTL3V1";

    /** BEAN_SUCCESS. */
    private static final Boolean BEAN_SUCCESS = Boolean.TRUE;

    /** BEAN_NOTES_SUCCESS. */
    private static final String BEAN_NOTES_SUCCESS = "notesSuccess";

    /** BEAN_NOTES_FAILURE. */
    private static final String BEAN_NOTES_FAILURE = "notesFailure";

    /** BEAN_PLATFORM. */
    private static final String BEAN_PLATFORM = "platform";

    /** tempFolder. */
    private static String tempFolder;

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Method called before any JUnit test is executed. Create new temporary directory. In case there
     * is already one, delete all old files and the directory.
     */
    @BeforeClass
    public static final void beforeClass() {

        final String tmpDir = System.getProperty("java.io.tmpdir");
        final File folder = new File(tmpDir, "citrusJson");

        assertTrue(FileUtils.delete(folder));

        if (!folder.mkdirs()) {

            fail("Could not create folder [ " + folder.getAbsolutePath() + " ]");
        }

        tempFolder = folder.getAbsolutePath();
        System.out.println("TempFolder: " + tempFolder);
    }

    /**
     * Method called at the end of this JUnit test.
     */
    @AfterClass
    public static final void afterClass() {

        final File folder = new File(tempFolder);
        assertTrue(FileUtils.delete(folder));
    }

    /**
     * Test for constructor.
     */
    @Test
    public void testCitrusTestLinkFileImpl() {

        final CitrusTestLinkFileHandlerImpl fileImpl = new CitrusTestLinkFileHandlerImpl();
        assertNotNull(fileImpl);
    }

    /**
     * Test for
     * {@link CitrusTestLinkFileHandlerImpl#writeToFile(com.consol.citrus.testlink.CitrusTestLinkBean, String)}
     * method.
     *
     * @throws Exception
     *             Thrown in case of an error.
     */
    @Test
    public void testWriteToFile() throws Exception {

        final CitrusTestLinkFileHandlerImpl fileImpl = new CitrusTestLinkFileHandlerImpl();
        assertNotNull(fileImpl);

        fileImpl.writeToFile(null, null);
        fileImpl.writeToFile(this.createCitrusTestLinkBean(), tempFolder);
    }

    /**
     * Test for {@link CitrusTestLinkFileHandlerImpl#readFromDirectory(String)} method.
     *
     * <p>
     * <b>Make sure to excecute this test, after something was written into the {@link #tempFolder}
     * !</b>
     * </p>
     *
     * @throws Exception
     *             Thrown in case of an error.
     */
    @Test
    public void testReadFromDirectory() throws Exception {

        final CitrusTestLinkFileHandlerImpl fileImpl = new CitrusTestLinkFileHandlerImpl();
        assertNotNull(fileImpl);

        fileImpl.readFromDirectory(null);

        final List<CitrusTestLinkFileBean> result = fileImpl.readFromDirectory(tempFolder);
        assertNotNull(result);
        assertTrue(1 == result.size());
        assertEquals(this.createCitrusTestLinkBean(), result.get(0).getBean());
    }

    /**
     * Create a new {@link CitrusTestLinkBean} element. Needed to verify the reading.
     *
     * @return Newly created CITRUS test bean.
     */
    private CitrusTestLinkBean createCitrusTestLinkBean() {

        final CitrusTestLinkBean bean = new CitrusTestLinkBean();
        assertNotNull(bean);

        bean.setResponseState(BEAN_SUCCESS);

        final TestCase tcase = new TestCase();
        assertNotNull(tcase);

        bean.setId("uuid");

        tcase.setName(TEST_CASE_NAME);
        bean.setCitrusTestCase(tcase);

        bean.setStartTime(0L);
        bean.setEndTime(1L);

        bean.setBuildId(BEAN_BUILD_ID);
        bean.setBuildName(BEAN_BUILD_NAME);
        bean.setNotesFailure(BEAN_NOTES_FAILURE);
        bean.setNotesSuccess(BEAN_NOTES_SUCCESS);
        bean.setPlatform(BEAN_PLATFORM);
        bean.setSuccess(BEAN_SUCCESS);
        bean.setTestCaseId(BEAN_TEST_CASE_ID);
        bean.setTestCaseInternalId(BEAN_TEST_CASE_INTERNAL_ID);
        bean.setTestPlanId(BEAN_TEST_PLAN_ID);

        return bean;
    }

}
