package org.chemaster.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.chemaster.db.exception.DbException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class ResultSetIterator extends DbIterator<String> {

    public ResultSetIterator(final ResultSet rs) {
        super(rs);
    }

    @Override
    public String next() throws DbException{
        try {
            return rs.getString(1);
        } catch (SQLException ex) {
            throw new DbException(ex);
        }
    }
    
    

}
