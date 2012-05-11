package org.chemaster.db;

import org.chemaster.db.exception.DbException;


/**
 * Updates an already existing object in the database
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public abstract  class DbUpdater extends DbOperation {
    

    public DbUpdater() {
        super();
    }

    @Override
    public String getSqlTemplate() {
        String SQL_TEMPLATE = "UPDATE % SET % %";
        return SQL_TEMPLATE;
    }

    public abstract int update() throws DbException;
    
    

}
