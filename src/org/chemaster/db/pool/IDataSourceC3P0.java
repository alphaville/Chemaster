package org.chemaster.db.pool;

import javax.sql.DataSource;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public interface IDataSourceC3P0 {

    void close() throws Exception;

    DataSource getDatasource();

    String getTicket();
}
