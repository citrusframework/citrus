/*
 * Copyright the original author or authors.
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
 */

package org.citrusframework.camel.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.camel.actions.infra.CamelInfraSettings;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "run",
        "stop"
})
public class Infra {

    @XmlElement
    protected Run run;
    @XmlElement
    protected Stop stop;

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Run {

        @XmlAttribute
        private String catalog;
        @XmlAttribute(required = true)
        private String service;
        @XmlAttribute
        private String implementation;
        @XmlAttribute(name = "auto-remove")
        private boolean autoRemove = CamelInfraSettings.isAutoRemoveServices();

        @XmlAttribute(name = "dump-service-output")
        private boolean dumpServiceOutput = CamelInfraSettings.isDumpServiceOutput();

        public String getCatalog() {
            return catalog;
        }

        public void setCatalog(String catalog) {
            this.catalog = catalog;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getImplementation() {
            return implementation;
        }

        public void setImplementation(String implementation) {
            this.implementation = implementation;
        }

        public boolean isAutoRemove() {
            return autoRemove;
        }

        public void setAutoRemove(boolean autoRemove) {
            this.autoRemove = autoRemove;
        }

        public boolean isDumpServiceOutput() {
            return dumpServiceOutput;
        }

        public void setDumpServiceOutput(boolean dumpServiceOutput) {
            this.dumpServiceOutput = dumpServiceOutput;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Stop {

        @XmlAttribute
        private String service;
        @XmlAttribute
        private String implementation;

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getImplementation() {
            return implementation;
        }

        public void setImplementation(String implementation) {
            this.implementation = implementation;
        }
    }
}
