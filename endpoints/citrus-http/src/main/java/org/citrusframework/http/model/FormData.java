/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.http.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Christoph Deppisch
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "contentType",
        "action",
        "controls"
})
@XmlRootElement(name = "form-data")
public class FormData {

    @XmlElement(name = "content-type")
    protected String contentType;

    @XmlElement
    protected String action;

    @XmlElement(required = true)
    protected FormData.Controls controls;

    /**
     * Gets the content type.
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the action.
     * @return
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action.
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Gets the controls.
     * @return
     */
    public FormData.Controls getControls() {
        return controls;
    }

    /**
     * Sets the controls.
     * @param controls
     */
    public void setControls(FormData.Controls controls) {
        this.controls = controls;
    }

    /**
     * Adds new form control.
     * @param control
     */
    public void addControl(Control control) {
        if (controls == null) {
            controls = new FormData.Controls();
        }

        this.controls.add(control);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "controls"
    })
    @XmlSeeAlso({
            Control.class
    })
    public static class Controls {
        @XmlElement(name = "control", required = true)
        protected List<Control> controls = new ArrayList<Control>();

        public List<Control> getControls() {
            return this.controls;
        }

        public void add(Control control) {
            this.controls.add(control);
        }
    }
}
