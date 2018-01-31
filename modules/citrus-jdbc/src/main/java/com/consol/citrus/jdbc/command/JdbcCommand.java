package com.consol.citrus.jdbc.command;

import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.jdbc.model.TransactionCommitted;
import com.consol.citrus.jdbc.model.TransactionRollback;
import com.consol.citrus.jdbc.model.TransactionStarted;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.springframework.xml.transform.StringResult;

/**
 * JdbcCommands represent the technical part of the database communication
 */
public class JdbcCommand extends DefaultMessage {

    private JdbcMarshaller marshaller = new JdbcMarshaller();
    private Operation operation;

    /**
     * Prevent traditional instantiation.
     */
    private JdbcCommand() { super(); }

    /**
     * Constructor initializes new JDBC command.
     * @param operation The operation to be executed
     */
    private JdbcCommand(final Operation operation) {
        super(operation);
        this.operation = operation;
    }

    @Override
    public <T> T getPayload(final Class<T> type) {
        if (String.class.equals(type)) {
            return (T) getPayload();
        } else {
            return super.getPayload(type);
        }
    }

    @Override
    public Object getPayload() {
        final StringResult payloadResult = new StringResult();
        if (operation != null) {
            marshaller.marshal(operation, payloadResult);
            return payloadResult.toString();
        }

        return super.getPayload();
    }

    public static Message startTransaction() {
        return new JdbcCommand(new Operation(new TransactionStarted()));
    }

    public static Message commitTransaction(){
        return new JdbcCommand(new Operation(new TransactionCommitted()));
    }

    public static Message rollbackTransaction(){
        return new JdbcCommand(new Operation(new TransactionRollback()));
    }
}
