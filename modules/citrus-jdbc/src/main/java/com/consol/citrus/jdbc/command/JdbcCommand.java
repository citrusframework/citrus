package com.consol.citrus.jdbc.command;

import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.jdbc.model.TransactionCommitted;
import com.consol.citrus.jdbc.model.TransactionRollback;
import com.consol.citrus.jdbc.model.TransactionStarted;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;

/**
 * JdbcCommands represent the technical part of the database communication
 */
public class JdbcCommand extends DefaultMessage {


    public static final Message TRANSACTION_STARTED = new JdbcCommand(new Operation(new TransactionStarted()));
    public static final Message TRANSACTION_COMMITTED = new JdbcCommand(new Operation(new TransactionCommitted()));
    public static final Message TRANSACTION_ROLLBACK = new JdbcCommand(new Operation(new TransactionRollback()));

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
    }


}
