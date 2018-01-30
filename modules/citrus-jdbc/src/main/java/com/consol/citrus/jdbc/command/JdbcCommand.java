package com.consol.citrus.jdbc.command;

import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;

/**
 * JdbcCommands represent the technical part of the database communication
 */
public class JdbcCommand extends DefaultMessage {


    public static final Message TRANSACTION_STARTED = new JdbcCommand();
    public static final Message TRANSACTION_COMMITTED = new JdbcCommand();
    public static final Message TRANSACTION_ROLLBACK = new JdbcCommand();

    private Operation operation;

    private JdbcMarshaller marshaller = new JdbcMarshaller();

    /**
     * Prevent traditional instantiation.
     */
    private JdbcCommand() { super(); }

    /**
     * Constructor initializes new JDBC command.
     * @param operation The operation to be executed
     */
    private JdbcCommand(Operation operation) {
        super(operation);
        this.operation = operation;
    }


}
