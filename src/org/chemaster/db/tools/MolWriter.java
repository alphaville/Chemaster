package org.chemaster.db.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chemaster.client.ServiceInvocationException;
import org.chemaster.db.DbWriter;
import org.chemaster.db.exception.DbException;
import org.chemaster.db.pool.DataSourceFactory;
import org.chemaster.pubchem.InchiKeyToIUPACName;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.XYZWriter;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;

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
    private File file;
    private URI sourceUri;
    private final String insertCompound = "INSERT IGNORE INTO `Compound` "
            + "(`inchikey`,`uploadedAs`,`source`)"
            + " VALUES (?,?,?)";
    private final String insertRepresentation = "INSERT IGNORE INTO `Representation` (`name`,`content`,`compound`) VALUES (?,?,?)";
    private final String insertProp = "INSERT IGNORE INTO `PropertyValue` (`property`,`compound`,`str_value`,`comment`) VALUES (?,?,?,?)";
    private final String insertNumericProp = "INSERT IGNORE INTO `PropertyValue` (`property`,`compound`,`dbl_value`,`comment`) VALUES (?,?,?,?)";
    private PreparedStatement insertCompoundStatement = null;
    private PreparedStatement insertRepresentationStatement = null;
    private PreparedStatement insertPropStatement = null;
    private PreparedStatement insertNumericPropStatement = null;
    /**
     * List of representation one needs to  
     * store in the database.
     */
    private List<String> representationsList = new ArrayList<String>() {

        {
            add("sdf");
            add("smiles");
            add("inchi");
            add("xyz");
        }
    };
    private IteratingMDLReader rdr = null;

    /**
     * Create a new instance of <code>MolWriter</code> which will be used
     * to write an SD file or other file with molecules into the database.
     * 
     * @param path
     *      Path to the SD File.
     * @throws FileNotFoundException 
     *      If the file was not found under the specified path.
     */
    public MolWriter(String path) throws FileNotFoundException {
        file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("The file: '" + file.getName() + "' was not "
                    + "found at the specified location: '" + file.getAbsolutePath() + "'.");
        }
        sourceUri = file.toURI();
        FileReader fr = new FileReader(file);
        IChemObjectBuilder ob = DefaultChemObjectBuilder.getInstance();
        rdr = new IteratingMDLReader(fr, ob);
    }

    public MolWriter(IteratingMDLReader rdr) {
        this.rdr = rdr;
    }

    /**
     * List of representations that you need to be registered in the
     * database. Admissible values are sdf, smiles, inchi and xyz.
     * @return 
     *      List of representations.
     */
    public List<String> getRepresentations() {
        return representationsList;
    }

    public boolean addRepresentation(String rep) {
        return representationsList.add(rep.toLowerCase());
    }

    @Override
    public int write() throws DbException {
        Connection connection = getConnection();

        FileInputStream fileInputStream = null;
        // BAOS used to write the SDF representation of every molecule
        ByteArrayOutputStream baos_sdf = null;

        try {
            connection.setAutoCommit(false);

            insertCompoundStatement = connection.prepareStatement(insertCompound);
            insertRepresentationStatement = connection.prepareStatement(insertRepresentation);
            insertPropStatement = connection.prepareStatement(insertProp);
            insertNumericPropStatement = connection.prepareStatement(insertNumericProp);

            String currentMolName = null;
            String currentInchiKey = null;
            String currentInchi = null;

            /*
             * Iterate over all molecules in the provided file
             */
            while (rdr.hasNext()) {
                IAtomContainer currentMol = rdr.next();
                baos_sdf = new ByteArrayOutputStream();
                IChemObjectWriter writer = new SDFWriter(baos_sdf);
                writer.write(currentMol);
                writer.close();

                /*
                 * Get the InChiKey
                 */
                InChIGenerator gen = InChIGeneratorFactory.getInstance().
                        getInChIGenerator(currentMol);
                currentInchiKey = gen.getInchiKey();
                currentInchi = gen.getInchi();

                /*
                 * Get the name (IUPAC)
                 */
                boolean nameProvided = false;
                Object nameObject = currentMol.getProperty("PUBCHEM_IUPAC_NAME");
                System.out.println(nameObject);
                if (nameObject != null) {
                    currentMolName = nameObject.toString();
                    nameProvided = true;
                } else {
                    currentMolName = null;
                    nameObject = currentMol.getProperty("cdk:Title");

                    if (nameObject != null) {
                        currentMolName = nameObject.toString();
                    } else {
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
                }

                System.out.println("--- Molecule Registration ");
                System.out.println("* Name::" + currentMolName);
                System.out.println("* InChiKey::" + currentInchiKey);
                System.out.println();

                /*
                 * Register the Compound in the `Compound` table
                 */
                insertCompoundStatement.setString(1, currentInchiKey);
                insertCompoundStatement.setString(2, "sdf");
                insertCompoundStatement.setString(3, sourceUri.toString());
                insertCompoundStatement.addBatch();
                insertCompoundStatement.clearParameters();

                /*
                 * Register the IUPAC Name
                 */
                if (currentMolName != null) {
                    insertPropStatement.setString(1, "IUPAC Name");
                    insertPropStatement.setString(2, currentInchiKey);
                    insertPropStatement.setString(3, currentMolName);
                    insertPropStatement.setString(4, nameProvided ? "Name provided by the end-user" : "Name retrieved from PubChem");
                    insertPropStatement.addBatch();
                    insertPropStatement.clearParameters();
                }


                if (this.representationsList.contains("sdf")) {
                    insertRepresentationStatement.setString(1, "sdf");
                    insertRepresentationStatement.setString(2, baos_sdf.toString());
                    insertRepresentationStatement.setString(3, currentInchiKey);
                    insertRepresentationStatement.addBatch();
                    insertRepresentationStatement.clearParameters();
                }
                if (this.representationsList.contains("smiles")) {
                    SmilesGenerator smi = new SmilesGenerator(true);
                    smi.setUseAromaticityFlag(true);
                    String smiles = smi.createSMILES(currentMol);
                    insertRepresentationStatement.setString(1, "smiles");
                    insertRepresentationStatement.setString(2, smiles);
                    insertRepresentationStatement.setString(3, currentInchiKey);
                    insertRepresentationStatement.addBatch();
                    insertRepresentationStatement.clearParameters();
                }
                if (this.representationsList.contains("xyz")) {
                    ByteArrayOutputStream baos_xyz = new ByteArrayOutputStream();
                    XYZWriter xyz = new XYZWriter(baos_xyz);
                    xyz.write(currentMol);
                    xyz.close();
                    baos_sdf.close();
                    insertRepresentationStatement.setString(1, "xyz");
                    insertRepresentationStatement.setString(2, baos_xyz.toString());
                    insertRepresentationStatement.setString(3, currentInchiKey);
                    insertRepresentationStatement.addBatch();
                    insertRepresentationStatement.clearParameters();
                }
                if (this.representationsList.contains("inchi")) {
                    insertRepresentationStatement.setString(1, "inchi");
                    insertRepresentationStatement.setString(2, currentInchi);
                    insertRepresentationStatement.setString(3, currentInchiKey);
                    insertRepresentationStatement.addBatch();
                    insertRepresentationStatement.clearParameters();
                }


                Object traditionalNameObj = currentMol.getProperty("PUBCHEM_IUPAC_TRADITIONAL_NAME");
                if (traditionalNameObj != null && !traditionalNameObj.toString().trim().isEmpty()) {
                    insertPropStatement.setString(1, "Traditional Name");
                    insertPropStatement.setString(2, currentInchiKey);
                    insertPropStatement.setString(3, traditionalNameObj.toString().trim());
                    insertPropStatement.setString(4, "");
                    insertPropStatement.addBatch();
                    insertPropStatement.clearParameters();

                }

                Object cidObj = currentMol.getProperty("PUBCHEM_COMPOUND_CID");
                if (cidObj != null && !cidObj.toString().trim().isEmpty()) {
                    insertNumericPropStatement.setString(1, "CID");
                    insertNumericPropStatement.setString(2, currentInchiKey);
                    insertNumericPropStatement.setInt(3, Integer.parseInt(cidObj.toString().trim()));
                    insertNumericPropStatement.setString(4, "");
                    insertNumericPropStatement.addBatch();
                    insertNumericPropStatement.clearParameters();
                }

            }
            insertCompoundStatement.executeBatch();
            insertRepresentationStatement.executeBatch();
            insertPropStatement.executeBatch();
            insertNumericPropStatement.executeBatch();
            connection.commit();

            return 0;
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
            if (insertPropStatement != null) {
                try {
                    insertPropStatement.close();
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

    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
    }

    public static void main(String... e) throws Exception {
//        while(true){
//            Thread.sleep(100);
//            System.out.println("("+MouseInfo.getPointerInfo().getLocation().x+", "+MouseInfo.getPointerInfo().getLocation().y+")");
//        }
        String[] paths = {"mols.sdf", "first.sdf", "__mols.sdf"};
        MolWriter my_writer;
        for (String path : paths) {
            my_writer = new MolWriter(path);
            my_writer.write();
            my_writer.close();
        }

        DataSourceFactory.getInstance().close();
    }
}
