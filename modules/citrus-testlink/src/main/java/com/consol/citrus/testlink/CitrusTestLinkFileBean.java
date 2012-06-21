/*
 * File: CitrusTestLinkFileBean.java
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
 * last modified: Friday, May 18, 2012 (17:53) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.io.File;

/**
 * Bean for holding a {@link CitrusTestLinkBean} element and a {@link File} element.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class CitrusTestLinkFileBean {

    // ~ Instance fields -------------------------------------------------------------------------

    /** bean. */
    private final CitrusTestLinkBean bean;

    /** file. */
    private final File file;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkFileBean} class.
     *
     * @param beanIn
     *            Bean element.
     * @param fileIn
     *            File element.
     */
    public CitrusTestLinkFileBean(final CitrusTestLinkBean beanIn, final File fileIn) {

        super();

        this.bean = beanIn;
        this.file = fileIn;
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code bean} field.
     *
     * @return {@code bean} field.
     */
    public CitrusTestLinkBean getBean() {

        return this.bean;
    }

    /**
     * Returns the value of the {@code file} field.
     *
     * @return {@code file} field.
     */
    public File getFile() {

        return this.file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.bean == null) ? 0 : this.bean.hashCode());
        result = (prime * result) + ((this.file == null) ? 0 : this.file.hashCode());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final CitrusTestLinkFileBean other = (CitrusTestLinkFileBean) obj;

        if (this.bean == null) {

            if (other.bean != null) {
                return false;
            }
        } else if (!this.bean.equals(other.bean)) {
            return false;
        }

        if (this.file == null) {

            if (other.file != null) {
                return false;
            }
        } else if (!this.file.equals(other.file)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("CitrusTestLinkFileBean [bean=");
        builder.append(this.bean);
        builder.append(", file=");
        builder.append(this.file);
        builder.append("]");

        return builder.toString();
    }

}
