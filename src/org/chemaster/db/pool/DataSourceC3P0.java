package org.chemaster.db.pool;

import javax.sql.DataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.util.UUID;
import org.chemaster.db.exception.DbException;
import org.chemaster.db.global.DbConfiguration;

class DataSourceC3P0 implements IDataSourceC3P0 {

    protected volatile ComboPooledDataSource datasource;
    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataSourceC3P0.class);
    private static final String ticket = UUID.randomUUID().toString();

    /**
     * Default configuration
     * @throws DbException
     */
    public DataSourceC3P0() throws DbException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
        } catch (final ClassNotFoundException ex) {
            final String msg = "Driver com.mysql.jdbc.Driver not found";
            logger.error(msg,ex);
            throw new DbException(msg, ex);
        }
        datasource = new ComboPooledDataSource();  // create a new datasource object
        datasource.setProperties(DbConfiguration.getInstance().getProperpties());
        logger.info("Acquired datasource [".concat(ticket).concat("]".concat(" with properties... ".concat(datasource.getProperties().toString()))));

    }

    public DataSourceC3P0(String connectURI) throws DbException {
        this();
        datasource.setJdbcUrl(connectURI);
    }

    @Override
    public String getTicket() {
        return ticket;
    }

    @Override
    public void close() throws DbException {
        if (datasource != null) {
            try {
                datasource.close();
            } catch (final Exception ex) {
                final String msg = "Unexpected exception while closing datasource";
                logger.warn(msg, ex);
                throw new DbException(msg, ex);
            } catch (final Error ex) {
                final String msg = "Unexpected error while closing datasource";
                logger.warn(msg, ex);
                throw new DbException(msg, ex);
            }
        }

    }

    @Override
    public DataSource getDatasource() {
        return datasource;
    }
}
