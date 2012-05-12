package org.chemaster.db.tools;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chemaster.db.DbWriter;
import org.chemaster.db.exception.DbException;

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

    private final String filePath;
    private final String insertCompound = "INSERT IGNORE INTO `Compound` (`inchikey`,`uploadedAs`,`source`) VALUES (?,?,?)";
    private final String insertRepresentation = "INSERT IGNORE INTO `Representation` (`name`,`content`,`compound`) VALUES (?,?,?)";
    private final String insertName = "INSERT IGNORE INTO `PropertyValue` (`property`,`compound`,`str_value`) VALUES ('IUPAC Name',?,?)";
   
    private PreparedStatement insertCompoundStatement = null;
    private PreparedStatement insertRepresentationStatement = null;
    private PreparedStatement insertIUPACStatement = null;
    private List<String> list = new ArrayList<String>() {

        {
            add("sdf");
            add("smiles");
            add("inchi");
        }
    };

    public MolWriter(String path) {
        this.filePath = path;
    }

    public List<String> getRepresentations() {
        return list;
    }

    public boolean addRepresentation(String rep) {
        return list.add(rep);
    }

    private void writeRepresentation(final Molecule molecule, final String inchiKey,
            final String representation) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MolExporter representationExporter = new MolExporter(baos, representation);
            representationExporter.write(molecule);
            insertRepresentationStatement.setString(1, representation);
            insertRepresentationStatement.setString(2, baos.toString().trim());
            insertRepresentationStatement.setString(3, inchiKey);
            insertRepresentationStatement.addBatch();
            baos.flush();
            representationExporter.close();
        } catch (Exception ex) {
            Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int write() throws DbException {

        Connection connection = getConnection();
        MolImporter molImporter = null;
        ByteArrayOutputStream baos = null;

        try {
            insertCompoundStatement = connection.prepareStatement(insertCompound);
            insertRepresentationStatement = connection.prepareStatement(insertRepresentation);
            insertIUPACStatement = connection.prepareStatement(insertName);

            FileInputStream fileInputStream = new FileInputStream(filePath);
            molImporter = new MolImporter(fileInputStream);
            Molecule molecule;

            MolExporter inchiKeyExporter = null;

            while ((molecule = molImporter.read()) != null) {
                baos = new ByteArrayOutputStream();
                inchiKeyExporter = new MolExporter(baos, "inchikey");
                inchiKeyExporter.write(molecule);
                String inchiKey = baos.toString().trim().split("=")[1];
                insertCompoundStatement.setString(1, inchiKey);
                insertCompoundStatement.setString(2, "sdf");
                insertCompoundStatement.setString(3, "FILE " + filePath);
                insertCompoundStatement.addBatch();
                baos.flush();
                /*
                 * How to get the IUPAC Name:
                 * http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/BPGDAMSIGCZZLK-UHFFFAOYSA-N/
                 */

                for (String r : list) {
                    writeRepresentation(molecule, inchiKey, r);
                }
             
                insertIUPACStatement.setString(1, inchiKey);
                insertIUPACStatement.setString(2, molecule.getName().trim());
                insertIUPACStatement.addBatch();               

            }
            insertCompoundStatement.executeBatch();
            insertRepresentationStatement.executeBatch();
            insertIUPACStatement.executeBatch();

            inchiKeyExporter.close();

        } catch (SQLException ex) {
            Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                insertCompoundStatement.close();
            } catch (SQLException ex) {
                Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                molImporter.close();
            } catch (IOException ex) {
                Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                baos.close();
            } catch (IOException ex) {
                Logger.getLogger(MolWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 0;
    }

    public static void main(String... e) throws Exception {
        String path = "/home/chung/Desktop/first.sdf";
        MolWriter my_writer = new MolWriter(path);
        my_writer.write();
        my_writer.close();
    }
}
