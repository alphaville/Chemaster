package org.chemaster.db.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.chemaster.core.ICompound;
import org.chemaster.core.ICoupleCompounds;
import org.chemaster.core.impl.CoupleCompounds;
import org.chemaster.db.AbstractDbOperation;
import org.chemaster.db.IDbIterator;
import org.chemaster.db.exception.DbException;
import org.chemaster.db.query.compound.CompoundReader;
import org.openscience.cdk.exception.CDKException;

/**
 *
 * Updates the finderprints for all molecules in the database
 */
public class SimilarityUpdater extends AbstractDbOperation {

    private static final String __insert_similarity =
            "INSERT IGNORE INTO `Similarity` "
            + "(`hash`,`compound1`,`compound2`,`tanimoto`,`ext_tanimoto`,`estate_tanimoto`) "
            + "VALUES (?,?,?,?,?,?)";
    private PreparedStatement insertSimilarityStatement;

    public SimilarityUpdater() throws DbException {
        super();
        init();
    }

    public SimilarityUpdater(Connection connection) {
        super(connection);
    }

    private void init() throws DbException {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
            insertSimilarityStatement = connection.prepareStatement(__insert_similarity);
        } catch (SQLException ex) {
            throw new DbException("SQL Exception", ex);
        }
    }

    public void addCompoundPair(ICoupleCompounds couple) throws DbException {
        try {
            insertSimilarityStatement.setString(1, couple.hashKey());
            ICompound[] compounds = couple.getCompounds();
            if (compounds == null || (compounds != null && compounds.length != 2)) {
                throw new RuntimeException("ICoupleCompounds with improper number of compounds");
            }
            insertSimilarityStatement.setString(2, compounds[0].getInchiKey());
            insertSimilarityStatement.setString(3, compounds[1].getInchiKey());
            if (Double.isNaN(
                    couple.getSimilarity())) {
                insertSimilarityStatement.setNull(4, java.sql.Types.DOUBLE);
            } else {
                insertSimilarityStatement.setDouble(4, couple.getSimilarity());
            }
            if (Double.isNaN(
                    couple.getExtendedSimilarity())) {
                insertSimilarityStatement.setNull(5, java.sql.Types.DOUBLE);
            } else {
                insertSimilarityStatement.setDouble(5, couple.getExtendedSimilarity());
            }
            if (Double.isNaN(
                    couple.getESTATESimilarity())) {
                insertSimilarityStatement.setNull(6, java.sql.Types.DOUBLE);
            } else {
                insertSimilarityStatement.setDouble(6, couple.getESTATESimilarity());
            }

            insertSimilarityStatement.addBatch();
            insertSimilarityStatement.clearParameters();
        } catch (CDKException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new DbException(ex);
        }
    }

    public int submit() throws DbException {
        try {
            int[] res = insertSimilarityStatement.executeBatch();
            getConnection().commit();
            int i = 0;
            for (int r : res) {
                i += r;
            }
            return i;
        } catch (final SQLException ex) {
            throw new DbException(ex);
        }
    }

    public static void main(String... args) throws Exception {
        CompoundReader cr = new CompoundReader();
        CompoundReader cr2 = new CompoundReader();
        SimilarityUpdater sim = new SimilarityUpdater();

        IDbIterator<ICompound> iter = cr.list();
        int n = 0;
        while (iter.hasNext()) {            
            ICompound c = iter.next();
            IDbIterator<ICompound> iter2 = cr2.list();
            while (iter2.hasNext()) {
                ICompound c2 = iter2.next();
                ICoupleCompounds couple = new CoupleCompounds(c, c2);
                if (!c.equals(c2)) {
                    double similarityIndex = couple.getSimilarity();
                    double extsimilarityIndex = couple.getExtendedSimilarity();
                    double estateSimilarityIndex = couple.getESTATESimilarity();
                    double threshold = 0.50;
                    if (similarityIndex > threshold
                            || extsimilarityIndex > threshold
                            || estateSimilarityIndex > threshold) {
                        sim.addCompoundPair(couple);
                        ++n;
                    }
                }
            }
            iter2.close();
        }
        sim.submit();
        iter.close();
        sim.close();
        System.out.println("Similarity Values added :" + (n/2));
    }
    /*
     * Example on how to find similar compounds:
     * SELECT compound1, compound2 from Similarity where tanimoto>0.8
     */
}