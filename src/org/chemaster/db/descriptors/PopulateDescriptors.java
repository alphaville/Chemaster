package org.chemaster.db.descriptors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chemaster.db.DbWriter;
import org.chemaster.db.exception.DbException;
import org.chemaster.db.pool.DataSourceFactory;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

/**
 *
 * @author Pantelis Sopasakis
 */
public class PopulateDescriptors extends DbWriter {

    private File sdfile;
    private File descriptorsFile;
    private static final String insertPropertyValue = "INSERT IGNORE INTO `PropertyValue` "
            + "(`property`,`compound`,`dbl_value`,`software`,`software_version`) VALUE (?,?,?,'Dragon 6','6.0.20')";
    private PreparedStatement insertPropertyStatement = null;

    public PopulateDescriptors(String sdfile, String descriptorsFile) throws FileNotFoundException {
        this.sdfile = new File(sdfile);
        this.descriptorsFile = new File(descriptorsFile);
        if (!this.sdfile.exists()) {
            throw new FileNotFoundException("The file '" + sdfile + "' was not found.");
        }
        if (!this.descriptorsFile.exists()) {
            throw new FileNotFoundException("The file '" + descriptorsFile + "' was not found.");
        }
    }

    @Override
    public int write() throws DbException {
        FileReader descriptorsReader = null;
        FileReader sdfReader = null;
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
            /*
             * Prepare Statements
             */
            insertPropertyStatement = connection.prepareStatement(insertPropertyValue);
            
            /*
             * Reader for the descriptors' file:
             */
            descriptorsReader = new FileReader(descriptorsFile);
            BufferedReader bDescReader = new BufferedReader(descriptorsReader);
            String descLine = null;
            // Header - List of descriptors:
            descLine = bDescReader.readLine();
            String[] headerTokens = descLine.split("\t");

            /*
             * Reader for SDF
             */
            sdfReader = new FileReader(sdfile);
            IChemObjectBuilder ob = DefaultChemObjectBuilder.getInstance();
            IteratingMDLReader rdr = new IteratingMDLReader(sdfReader, ob);

            String currentInchiKey = null;
            while (rdr.hasNext()) {
                IAtomContainer currentMol = rdr.next();
                /*
                 * Get the InChiKey
                 */
                InChIGenerator gen = InChIGeneratorFactory.getInstance().
                        getInChIGenerator(currentMol);
                currentInchiKey = gen.getInchiKey();

                String descFileLine = bDescReader.readLine();
                String[] descTokens = descFileLine.split("\t");

                String currentProperty = null;
                double currentVal = -1;
                for (int i = 2; i < headerTokens.length; i++) {
                    currentProperty = headerTokens[i];
                    currentVal = Double.parseDouble(descTokens[i]);
                    insertPropertyStatement.setString(1, currentProperty);
                    insertPropertyStatement.setString(2, currentInchiKey);
                    insertPropertyStatement.setDouble(3, currentVal);
                    insertPropertyStatement.addBatch();
                    insertPropertyStatement.clearParameters();
                }
            }
            insertPropertyStatement.executeBatch();
            connection.commit();
        } catch (SQLException ex) {
            Logger.getLogger(PopulateDescriptors.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CDKException ex) {
            Logger.getLogger(PopulateDescriptors.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PopulateDescriptors.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public static void main(String... args) throws DbException, FileNotFoundException {
        String sdFile = "complex_str.sdf";
        String descriptorsFile = "complex_descriptors.txt";
        PopulateDescriptors pd = new PopulateDescriptors(sdFile, descriptorsFile);
        pd.write();
        DataSourceFactory.getInstance().close();
    }
}
