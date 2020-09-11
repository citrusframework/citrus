/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.jdbc.data;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import com.consol.citrus.db.driver.dataset.DataSet;
import com.consol.citrus.db.driver.json.JsonDataSetProducer;
import com.consol.citrus.db.driver.xml.XmlDataSetProducer;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.jdbc.model.OperationResult;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.xml.StringSource;
import org.springframework.util.StringUtils;

public class DataSetCreator {

    /**
     * Converts Citrus result set representation to db driver model result set.
     * @param response The result set to convert
     * @return A DataSet the jdbc driver can understand
     */
    public DataSet createDataSet(final Message response, final MessageType messageType) {
        try {
            if (response.getPayload() instanceof DataSet) {
                return response.getPayload(DataSet.class);
            } else if (isReadyToMarshal(response, messageType)) {
                return marshalResponse(response, messageType);
            } else {
                return new DataSet();
            }
        } catch (final SQLException e) {
            throw new CitrusRuntimeException("Failed to read dataSet from response message", e);
        }
    }

    /**
     * Marshals the given message to the requested MessageType
     * @param response The response to marshal
     * @param messageType The requested MessageType
     * @return A DataSet representing the message
     * @throws SQLException In case the marshalling failed
     */
    private DataSet marshalResponse(final Message response, final MessageType messageType) throws SQLException {
        String dataSet = null;

        if (response instanceof JdbcMessage || response.getPayload() instanceof OperationResult) {
            dataSet = response.getPayload(OperationResult.class).getDataSet();
        } else {
            try {
                JdbcMarshaller jdbcMarshaller = new JdbcMarshaller();
                jdbcMarshaller.setType(messageType.name());
                Object object = jdbcMarshaller.unmarshal(new StringSource(response.getPayload(String.class)));
                if (object instanceof OperationResult && StringUtils.hasText(((OperationResult) object).getDataSet())) {
                    dataSet = ((OperationResult) object).getDataSet();
                }
            } catch (CitrusRuntimeException e) {
                dataSet = response.getPayload(String.class);
            }
        }

        if (isJsonResponse(messageType)) {
            return new JsonDataSetProducer(Optional.ofNullable(dataSet).orElse("[]")).produce();
        } else if (isXmlResponse(messageType)) {
            return new XmlDataSetProducer(Optional.ofNullable(dataSet).orElse("<dataset></dataset>")).produce();
        } else {
            throw new CitrusRuntimeException("Unable to create dataSet from data type " + messageType.name());
        }
    }

    private boolean isReadyToMarshal(final Message response, final MessageType messageType) {
        return response.getPayload() != null &&
                (response.getPayload() instanceof OperationResult || StringUtils.hasText(response.getPayload(String.class))) &&
                isKnownMessageType(messageType);
    }

    private boolean isKnownMessageType(final MessageType messageType) {
        return isXmlResponse(messageType) || isJsonResponse(messageType);
    }

    private boolean isXmlResponse(final MessageType messageType) {
        return Objects.equals(MessageType.XML, messageType);
    }

    private boolean isJsonResponse(final MessageType messageType) {
        return Objects.equals(MessageType.JSON, messageType);
    }
}
