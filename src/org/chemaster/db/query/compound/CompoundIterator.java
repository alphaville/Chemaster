package org.chemaster.db.query.compound;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chemaster.core.ICompound;
import org.chemaster.core.impl.Compound;
import org.chemaster.db.DbIterator;
import org.chemaster.db.exception.DbException;
import org.chemaster.util.BitSetParser;

/**
 *
 * @author chung
 */
public class CompoundIterator extends DbIterator<ICompound> {

    private String representation;

    public CompoundIterator(ResultSet rs, String representation) {
        super(rs);
        this.representation = representation;
    }

    @Override
    public ICompound next() throws DbException {
        ICompound nextCompound = null;
        try {
            nextCompound = new Compound();
            String compoundInchi = rs.getString("inchikey");
            nextCompound.setInchiKey(compoundInchi);
            String fPrint = rs.getString("fingerprint");
            BitSetParser bsp = new BitSetParser(fPrint);
            BitSet bs = bsp.parse();
            String content = rs.getString("content");
            nextCompound.getRepresentations().put(representation, content);
            nextCompound.setFingerprint(bs);
        } catch (SQLException ex) {
            Logger.getLogger(CompoundIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nextCompound;
    }
}
