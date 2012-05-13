package org.chemaster.db.tools;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chemaster.client.ServiceInvocationException;
import org.chemaster.db.DbWriter;
import org.chemaster.db.exception.DbException;
import org.chemaster.db.pool.DataSourceFactory;
import org.chemaster.pubchem.InchiKeyToIUPACName;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 *
 * User Provides : Path to SD File
 * The compounds are stored in the DB in different representations
 * along with their names as provided by the end user in the SDF file.
 * The IUPAC Name is stored under the property "IUPAC Name"
 * 
 * @author Pantelis Sopasakis
 */
public class MolWriter extends DbWriter {

    //TODO Use Log4J
    private final String filePath;
    private File file;
    private URI fileUri;
    private final String insertCompound = "INSERT IGNORE INTO `Compound` (`inchikey`,`uploadedAs`,`source`,`fingerprint`) VALUES (?,?,?,?)";
    private final String insertRepresentation = "INSERT IGNORE INTO `Representation` (`name`,`content`,`compound`) VALUES (?,?,?)";
    private final String insertName = "INSERT IGNORE INTO `PropertyValue` (`property`,`compound`,`str_value`,`comment`) VALUES ('IUPAC Name',?,?,?)";
    private PreparedStatement insertCompoundStatement = null;
    private PreparedStatement insertRepresentationStatement = null;
    private PreparedStatement insertIUPACStatement = null;
    /**
     * List of representation one needs to  
     * store in the database.
     */
    private List<String> representationsList = new ArrayList<String>() {

        {
            add("sdf");
            add("smiles");
            add("inchi");
        }
    };

    public MolWriter(String path) throws FileNotFoundException {
        this.filePath = path;
        file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("The file: '" + file.getName() + "' was not "
                    + "found at the specified location: '" + file.getAbsolutePath() + "'.");
        }
        fileUri = file.toURI();
    }

    public List<String> getRepresentations() {
        return representationsList;
    }

    public boolean addRepresentation(String rep) {
        return representationsList.add(rep);
    }

    @Override
    public int write() throws DbException {
        Connection connection = getConnection();

        FileInputStream fileInputStream = null;
        // BAOS used to write the SDF representation of every molecule
        ByteArrayOutputStream baos_sdf = null;

        try {
            connection.setAutoCommit(false);

            FileReader fr = new FileReader(file);
            IChemObjectBuilder ob = DefaultChemObjectBuilder.getInstance();
            IteratingMDLReader rdr = new IteratingMDLReader(fr, ob);

            insertCompoundStatement = connection.prepareStatement(insertCompound);
            insertRepresentationStatement = connection.prepareStatement(insertRepresentation);
            insertIUPACStatement = connection.prepareStatement(insertName);

            String currentMolName = null;
            String currentInchiKey = null;
            BitSet fPrint = null;

            /*
             * Iterate over all molecules in the provided file
             */
            while (rdr.hasNext()) {
                IAtomContainer currentMol = rdr.next();
                baos_sdf = new ByteArrayOutputStream();
                SDFWriter writer = new SDFWriter(baos_sdf);
                writer.write(currentMol);
                writer.close();

                /*
                 * Get the InChiKey
                 */
                InChIGenerator gen = InChIGeneratorFactory.getInstance().
                        getInChIGenerator(currentMol);
                currentInchiKey = gen.getInchiKey();

                /*
                 * Get the name (IUPAC)
                 */
                boolean nameProvided = false;
                Object nameObject = currentMol.getProperty("cdk:Title");
                if (nameObject != null) {
                    currentMolName = nameObject.toString();
                    nameProvided = true;
                } else {
                    currentMolName = null;
                    try {
                        InchiKeyToIUPACName inchiKeyFetcher = new InchiKeyToIUPACName(currentInchiKey);
                        currentMolName = inchiKeyFetcher.getIupacName();
                    } catch (ServiceInvocationException ex) {
                        ex.printStackTrace();
                        // All attempts failed
                        // Ask the user to determine a name...
                        //TODO Use System.in
                        Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                /*
                 * Fingerprint
                 */
                fPrint = new Fingerprinter().getFingerprint(currentMol);

                System.out.println("--- Molecule Registration ");
                System.out.println("* Name::" + currentMolName);
                System.out.println("* InChiKey::" + currentInchiKey);
                System.out.println();

                /*
                 * Register the Compound in the `Compound` table
                 */
                insertCompoundStatement.setString(1, currentInchiKey);
                insertCompoundStatement.setString(2, "sdf");
                insertCompoundStatement.setString(3, fileUri.toString());
                insertCompoundStatement.setString(4, fPrint != null ? fPrint.toString() : null);
                insertCompoundStatement.addBatch();
                insertCompoundStatement.clearParameters();

                /*
                 * Register the IUPAC Name
                 */
                if (currentMolName != null) {
                    insertIUPACStatement.setString(1, currentInchiKey);
                    insertIUPACStatement.setString(2, currentMolName);
                    insertIUPACStatement.setString(3, nameProvided ? "Name provided by the end-user" : "Name retrieved from PubChem");
                    insertIUPACStatement.addBatch();
                    insertIUPACStatement.clearParameters();
                }
                if (this.representationsList.contains("sdf")) {
                    insertRepresentationStatement.setString(1, "sdf");
                    insertRepresentationStatement.setString(2, baos_sdf.toString());
                    insertRepresentationStatement.setString(3, currentInchiKey);
                    insertRepresentationStatement.addBatch();
                    insertRepresentationStatement.clearParameters();
                }
            }
            insertCompoundStatement.executeBatch();
            insertIUPACStatement.executeBatch();
            insertRepresentationStatement.executeBatch();
            connection.commit();

        } catch (CDKException ex) {
            Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (final SQLException ex) {
            throw new DbException(ex);
        } catch (final IOException ex) {
            throw new RuntimeException("Unexpected IO Exception", ex);
        } finally {
            if (insertCompoundStatement != null) {
                try {
                    insertCompoundStatement.close();
                } catch (final SQLException ex) {
                    throw new DbException("SQLExcetpion while closing the INSERT statement for compound", ex);
                }
            }
            if (insertRepresentationStatement != null) {
                try {
                    insertRepresentationStatement.close();
                } catch (final SQLException ex) {
                    throw new DbException("SQLExcetpion while closing the INSERT statement for Representations", ex);
                }
            }
            if (insertIUPACStatement != null) {
                try {
                    insertIUPACStatement.close();
                } catch (final SQLException ex) {
                    throw new DbException("SQLExcetpion while closing the INSERT statement for IUPAC Name", ex);
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (final IOException ex) {
                    throw new RuntimeException("Unexpected condition while closing the FIS", ex);
                }
            }
            if (baos_sdf != null) {
                try {
                    baos_sdf.close();
                } catch (IOException ex) {
                    Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return 0;
    }

    public static void main(String... e) throws Exception {
        String path = "mols.sdf";
        MolWriter my_writer = new MolWriter(path);
        my_writer.write();
        my_writer.close();
        DataSourceFactory.getInstance().close();
    }
}
