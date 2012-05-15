package org.chemaster.db.query.compound;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chemaster.core.ICompound;
import org.chemaster.core.ICoupleCompounds;
import org.chemaster.core.impl.CoupleCompounds;
import org.chemaster.db.DbReader;
import org.chemaster.db.IDbIterator;
import org.chemaster.db.exception.DbException;
import org.openscience.cdk.exception.CDKException;

/**
 *
 * @author Pantelis Sopasakis
 */
public class CompoundReader extends DbReader<ICompound> {
//TODO check again - create tests - refactor - add functionality
    private String representation = "sdf";
    private Statement statement = null;
    private boolean doFetchRepresentation = true;

    private void constructWhereClause() {
        // Representations...
        if (doFetchRepresentation) {
            if (where == null) {
                setWhere("Representation.name='" + representation + "'");
            } else {
                setWhere(where + " OR Representation.name='" + representation + "'");
            }
        }
    }

    public CompoundReader() {
    }

    @Override
    public IDbIterator<ICompound> list() throws DbException {
        setTable("Compound");
        List<String> columns = new ArrayList<String>();
        columns.add("Compound.inchikey");
        if (doFetchRepresentation) {
            columns.add("Representation.content");
        }
        setTableColumns(columns);
        if (doFetchRepresentation) {
            setInnerJoin("Representation ON Compound.inchikey=Representation.compound");
        }
        constructWhereClause();
        Connection connection = getConnection();
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(getSql());
            CompoundIterator it = new CompoundIterator(rs, representation);
            return it;
        } catch (SQLException ex) {
            throw new DbException(ex);
        } finally {
            // Do Nothing:  The client is expected to close the statement and the connection
        }
    }

    public static void main(String... args) throws DbException {
        CompoundReader cr = new CompoundReader();
        CompoundReader cr2 = new CompoundReader();

        IDbIterator<ICompound> iter = cr.list();
        int n = 0;
        while (iter.hasNext()) {
            ++n;
            ICompound c = iter.next();
            IDbIterator<ICompound> iter2 = cr2.list();
            while (iter2.hasNext()) {
                ICompound c2 = iter2.next();
                ICoupleCompounds couple = new CoupleCompounds(c, c2);
                if (!c.equals(c2)) {
                    try {
                        System.out.println(couple.getSimilarity() > 0.8);
                    } catch (CDKException ex) {
                        throw new RuntimeException("Unequal bitsets???", ex);
                    }
                } else {
                    try {
                        System.out.println(couple.getSimilarity());
                    } catch (CDKException ex) {
                        Logger.getLogger(CompoundReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            iter2.close();
        }
        iter.close();
        System.out.println("ALL=" + n);
        
    }
}
