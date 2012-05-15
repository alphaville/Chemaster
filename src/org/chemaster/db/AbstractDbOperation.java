package org.chemaster.db;

import java.sql.Connection;
import java.sql.SQLException;
import org.chemaster.db.exception.DbException;
import org.chemaster.db.pool.DataSourceFactory;

/**
 *
 * @author chung
 */
public abstract class AbstractDbOperation {
    private volatile Connection connection;
    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractDbOperation.class);

    public AbstractDbOperation() {
    }

    public AbstractDbOperation(Connection connection) {
        this.connection = connection;
    }

    protected Connection getConnection() throws DbException {
        if (connection == null) {
            DataSourceFactory factory = DataSourceFactory.getInstance();
            try {
                connection = factory.getDataSource().getConnection();
            } catch (final SQLException ex) {
                final String msg = "Cannot get connection from the connection pool";
                logger.warn(msg, ex);
                throw new DbException(msg, ex);
            }
        }
        return connection;
    }

    public void close() throws DbException {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (final SQLException ex) {
                final String msg = "Connection to the database cannot be closed";
                logger.warn(msg, ex);
                throw new DbException(msg, ex);
            }
        }
    }
}
