/*
 * File: CitrusTestLinkFileHandlerImpl.java
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
 * last modified: Friday, May 18, 2012 (18:06) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.testlink.CitrusTestLinkBean;
import com.consol.citrus.testlink.CitrusTestLinkFileBean;
import com.consol.citrus.testlink.CitrusTestLinkFileHandler;
import com.consol.citrus.testlink.utils.ConvertUtils;
import com.consol.citrus.testlink.utils.FileUtils;

/**
 * Implementation of writing / reading a CITRUS test case result into / from a file, using JSON. Use
 * the JSON implementation of CITRUS.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public class CitrusTestLinkFileHandlerImpl implements CitrusTestLinkFileHandler {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CitrusTestLinkFileHandlerImpl.class);

    /** BEAN_ID. */
    private static final String BEAN_ID = "beanId";

    /** BEAN_TEST_CASE_ID. */
    private static final String BEAN_TEST_CASE_ID = "testCaseId";

    /** BEAN_TEST_CASE_INTERNAL_ID. */
    private static final String BEAN_TEST_CASE_INTERNAL_ID = "testCaseInternalId";

    /** BEAN_TEST_PLAN_ID. */
    private static final String BEAN_TEST_PLAN_ID = "testPlanId";

    /** BEAN_BUILD_ID. */
    private static final String BEAN_BUILD_ID = "buildId";

    /** BEAN_BUILD_NAME. */
    private static final String BEAN_BUILD_NAME = "buildName";

    /** BEAN_SUCCESS. */
    private static final String BEAN_SUCCESS = "success";

    /** BEAN_NOTES_SUCCESS. */
    private static final String BEAN_NOTES_SUCCESS = "notesSuccess";

    /** BEAN_NOTES_FAILURE. */
    private static final String BEAN_NOTES_FAILURE = "notesFailure";

    /** BEAN_PLATFORM. */
    private static final String BEAN_PLATFORM = "platform";

    /** BEAN_START_TIME. */
    private static final String BEAN_START_TIME = "startTime";

    /** BEAN_END_TIME. */
    private static final String BEAN_END_TIME = "endTime";

    /** FILE_NAME_REG_EXP. */
    private static final String FILE_NAME_REG_EXP = "^[\\w-]+(success|failure)\\.json$";

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkFileHandlerImpl} class.
     */
    public CitrusTestLinkFileHandlerImpl() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public List<CitrusTestLinkFileBean> readFromDirectory(final String directory) {

        final List<CitrusTestLinkFileBean> result = new ArrayList<CitrusTestLinkFileBean>();

        try {

            final List<File> fileList = FileUtils.readFiles(FileUtils.readDirectory(directory),
                    FILE_NAME_REG_EXP);

            for (final File file : fileList) {

                if (FileUtils.isValidFile(file)) {

                    final String json = this.readJsonFromFile(file);
                    final CitrusTestLinkBean bean = this.jsonToTest(json);

                    if (null != bean) {

                        result.add(new CitrusTestLinkFileBean(bean, file));
                    }
                }
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught for directory [ " + directory + " ]", ex);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void writeToFile(final CitrusTestLinkBean bean, final String directory) {

        // make sure bean is valid, directory is tested later
        if (null == bean) {

            // this might not happen, so just return
            return;
        }

        try {

            // build file from bean and directory
            final File file = this.buildFile(bean, directory);

            // build JSON string from bean
            final String json = this.testToJson(bean);

            // make sure there was no error
            if ((null == file) || ((null == json) || (json.isEmpty()))) {

                // there should be some error message previously
                return;
            }

            // write JSON content to file
            this.writeJsonToFile(file, json);
        } catch (final Exception ex) {

            LOGGER.error("Exception caught while trying to write CITRUS test case [ " + bean
                    + " ] to directory [ " + directory + " ]", ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param file
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String readJsonFromFile(final File file) {

        FileInputStream fis = null;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            fis = new FileInputStream(file);

            int content;

            while ((content = fis.read()) != -1) {

                baos.write(content);
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught for file [ " + file + " ]", ex);
        } finally {

            FileUtils.close(fis);
        }

        return baos.toString();
    }

    /**
     * Write the created JSON string to the given file.
     *
     * @param file
     *            File to write JSON string too.
     * @param json
     *            JSON string to write.
     *
     * @throws IOException
     *             Thrown in case of some IO error.
     */
    private void writeJsonToFile(final File file, final String json) throws IOException {

        FileOutputStream fos = null;

        try {

            fos = new FileOutputStream(file);
            fos.write(json.getBytes());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Writing JSON [ {} ] to file [ {} ]", json, file);
            }
        } finally {

            FileUtils.close(fos);
        }
    }

    /**
     * Build from the values in the CITRUS bean and the given directory the new file.
     *
     * @param bean
     *            Bean holding information to build the filename.
     * @param directory
     *            Directory where to store the file.
     *
     * @return Newly created file for this test case.
     *
     * @throws IOException
     *             Thrown in case the file already exists-
     */
    private File buildFile(final CitrusTestLinkBean bean, final String directory) throws IOException {

        // in case of an error an IOException will be thrown
        final File folder = FileUtils.createDirectory(directory);

        final StringBuilder builder = new StringBuilder(bean.getCitrusTestCase().getName());

        builder.append("-");
        builder.append(FileUtils.dateAsString(new Date()));

        if (bean.getSuccess().booleanValue()) {

            builder.append("-success");
        } else {

            builder.append("-failure");
        }

        builder.append(".json");

        final File file = new File(folder, builder.toString());

        if (file.exists()) {

            throw new IOException("File [ " + file.getAbsolutePath() + " ] already exists!");
        }

        return file;
    }

    /**
     * Convert bean content to a JSON string.
     *
     * @param bean
     *            Bean holding all data to be stored in the JSON string.
     *
     * @return String holding the bean content as a JSON string.
     */
    private String testToJson(final CitrusTestLinkBean bean) {

        final Map<String, Object> jsonMap = new HashMap<String, Object>();

        jsonMap.put(BEAN_ID, bean.getId());
        jsonMap.put(BEAN_BUILD_ID, bean.getBuildId());
        jsonMap.put(BEAN_BUILD_NAME, bean.getBuildName());
        jsonMap.put(BEAN_NOTES_FAILURE, bean.getNotesFailure());
        jsonMap.put(BEAN_NOTES_SUCCESS, bean.getNotesSuccess());
        jsonMap.put(BEAN_PLATFORM, bean.getPlatform());
        jsonMap.put(BEAN_SUCCESS, bean.getSuccess());
        jsonMap.put(BEAN_TEST_CASE_ID, bean.getTestCaseId());
        jsonMap.put(BEAN_TEST_CASE_INTERNAL_ID, bean.getTestCaseInternalId());
        jsonMap.put(BEAN_TEST_PLAN_ID, bean.getTestPlanId());
        jsonMap.put(BEAN_START_TIME, Long.valueOf(bean.getStartTime()));
        jsonMap.put(BEAN_END_TIME, Long.valueOf(bean.getEndTime()));

        return JSONObject.toJSONString(jsonMap);
    }

    /**
     * Convert JSON string back to a CITRUS test case element.
     *
     * @param json
     *            JSON string.
     *
     * @return Newly created CITRUS test case element.
     */
    private CitrusTestLinkBean jsonToTest(final String json) {

        if ((null == json) || (json.isEmpty())) {

            return null;
        }

        final JSONParser parser = new JSONParser();
        CitrusTestLinkBean bean = null;

        try {

            final Object obj = parser.parse(json);

            if (obj instanceof Map<?, ?>) {

                @SuppressWarnings("unchecked")
                final Map<String, Object> jsonMap = (Map<String, Object>) obj;

                if (!jsonMap.isEmpty()) {

                    bean = new CitrusTestLinkBean();

                    for (final Entry<String, Object> entry : jsonMap.entrySet()) {

                        final String key = entry.getKey();

                        if (BEAN_ID.equals(key)) {

                            bean.setId(ConvertUtils.convertToString(entry.getValue()));
                        } else if (BEAN_BUILD_ID.equals(key)) {

                            bean.setBuildId(ConvertUtils.convertToInteger(entry.getValue()));
                        } else if (BEAN_BUILD_NAME.equals(key)) {

                            bean.setBuildName(ConvertUtils.convertToString(entry.getValue()));
                        } else if (BEAN_NOTES_FAILURE.equals(key)) {

                            bean.setNotesFailure(ConvertUtils.convertToString(entry.getValue()));
                        } else if (BEAN_NOTES_SUCCESS.equals(key)) {

                            bean.setNotesSuccess(ConvertUtils.convertToString(entry.getValue()));
                        } else if (BEAN_PLATFORM.equals(key)) {

                            bean.setPlatform(ConvertUtils.convertToString(entry.getValue()));
                        } else if (BEAN_SUCCESS.equals(key)) {

                            bean.setSuccess(ConvertUtils.convertToBoolean(entry.getValue()));
                        } else if (BEAN_TEST_CASE_ID.equals(key)) {

                            bean.setTestCaseId(ConvertUtils.convertToInteger(entry.getValue()));
                        } else if (BEAN_TEST_CASE_INTERNAL_ID.equals(key)) {

                            bean.setTestCaseInternalId(ConvertUtils.convertToInteger(entry.getValue()));
                        } else if (BEAN_TEST_PLAN_ID.equals(key)) {

                            bean.setTestPlanId(ConvertUtils.convertToInteger(entry.getValue()));
                        } else if (BEAN_START_TIME.equals(key)) {

                            bean.setStartTime(ConvertUtils.convertToLong(entry.getValue())
                                    .longValue());
                        } else if (BEAN_END_TIME.equals(key)) {

                            bean.setEndTime(ConvertUtils.convertToLong(entry.getValue()).longValue());

                        } else {

                            LOGGER.error("Unknown entry [ {} ]", entry);
                        }
                    }
                } else {

                    LOGGER.error("Empty JSON object [ {} ]", json);
                }
            } else {

                LOGGER.error("JSON object [ {} ] is not of type [ JSONObject ]", obj);
            }
        } catch (final Exception ex) {

            LOGGER.error("Exception caught for JSON string [ " + json + " ]", ex);
        }

        return bean;
    }

}
