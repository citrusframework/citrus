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
import jakarta.xml.bind.annotation.XmlValue;
import org.citrusframework.camel.CamelSettings;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "run",
        "stop",
        "verify",
})
public class JBang {

    @XmlAttribute
    private String camelVersion;

    @XmlAttribute
    private String kameletsVersion;

    @XmlElement
    protected RunIntegration run;

    @XmlElement
    protected StopIntegration stop;

    @XmlElement
    protected VerifyIntegration verify;

    public void setCamelVersion(String camelVersion) {
        this.camelVersion = camelVersion;
    }

    public String getCamelVersion() {
        return camelVersion;
    }

    public void setKameletsVersion(String kameletsVersion) {
        this.kameletsVersion = kameletsVersion;
    }

    public String getKameletsVersion() {
        return kameletsVersion;
    }

    public void setRun(RunIntegration run) {
        this.run = run;
    }

    public RunIntegration getRun() {
        return run;
    }

    public void setStop(StopIntegration stop) {
        this.stop = stop;
    }

    public StopIntegration getStop() {
        return stop;
    }

    public void setVerify(VerifyIntegration verify) {
        this.verify = verify;
    }

    public VerifyIntegration getVerify() {
        return verify;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "integration"
    })
    public static class RunIntegration {

        @XmlElement(required = true)
        private Integration integration;

        public Integration getIntegration() {
            return integration;
        }

        public void setIntegration(Integration integration) {
            this.integration = integration;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Integration {
            @XmlAttribute
            protected String name;

            @XmlAttribute
            protected String file;

            @XmlValue
            protected String sourceCode;

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setFile(String file) {
                this.file = file;
            }

            public String getFile() {
                return file;
            }

            public void setSourceCode(String sourceCode) {
                this.sourceCode = sourceCode;
            }

            public String getSourceCode() {
                return sourceCode;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StopIntegration {
        @XmlAttribute
        protected String integration;

        public void setIntegration(String integration) {
            this.integration = integration;
        }

        public String getIntegration() {
            return integration;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class VerifyIntegration {
        @XmlAttribute
        protected String integration;

        @XmlAttribute(name = "log-message")
        private String logMessage;

        @XmlAttribute(name = "max-attempts")
        private int maxAttempts = CamelSettings.getMaxAttempts();
        @XmlAttribute(name = "delay-between-attempts")
        private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

        @XmlAttribute
        private String phase = "Running";
        @XmlAttribute(name = "print-logs")
        private boolean printLogs = CamelSettings.isPrintLogs();

        @XmlAttribute(name = "stop-on-error-status")
        private boolean stopOnErrorStatus = true;

        public void setIntegration(String integration) {
            this.integration = integration;
        }

        public String getIntegration() {
            return integration;
        }

        public void setLogMessage(String logMessage) {
            this.logMessage = logMessage;
        }

        public String getLogMessage() {
            return logMessage;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public String getPhase() {
            return phase;
        }

        public void setPrintLogs(boolean printLogs) {
            this.printLogs = printLogs;
        }

        public boolean isPrintLogs() {
            return printLogs;
        }

        public void setDelayBetweenAttempts(long delayBetweenAttempts) {
            this.delayBetweenAttempts = delayBetweenAttempts;
        }

        public long getDelayBetweenAttempts() {
            return delayBetweenAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setStopOnErrorStatus(boolean stopOnErrorStatus) {
            this.stopOnErrorStatus = stopOnErrorStatus;
        }

        public boolean isStopOnErrorStatus() {
            return stopOnErrorStatus;
        }
    }
}
