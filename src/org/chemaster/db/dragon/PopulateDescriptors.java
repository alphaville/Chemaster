package org.chemaster.db.dragon;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

/**
 *
 * @author Pantelis Sopasakis
 */
public class PopulateDescriptors extends DbWriter {

    private String sdfile;
    private String descriptorsFile;
    private static final String insertPropertyValue = "INSERT IGNORE INTO `PropertyValue` "
            + "(`property`,`compound`,`dbl_value`) VALUE (?,?,?)";
    private PreparedStatement insertPropertyStatement = null;

    public PopulateDescriptors(String sdfile, String descriptorsFile) {
        this.sdfile = sdfile;
        this.descriptorsFile = descriptorsFile;
    }

    @Override
    public int write() throws DbException {
        ByteArrayOutputStream baos = null;
        FileReader fr = null;
        MolImporter molImporter = null;
        MolExporter molExporter = null;
        FileInputStream fileInputStream = null;
        Connection connection = getConnection();

        try {
            insertPropertyStatement = connection.prepareStatement(insertPropertyValue);
            fileInputStream = new FileInputStream(sdfile);
            molImporter = new MolImporter(fileInputStream);
            Molecule molecule;

            File f = new File(descriptorsFile);
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String strLine = null;
            String firstLine = br.readLine();
            String[] header;

            if (firstLine != null) {
                header = firstLine.split("\t");

                while ((strLine = br.readLine()) != null) {
                    String[] tokens = strLine.split("\t");
                    molecule = molImporter.read();//current molecule
                    // Get the InChiKey of the current molecule:
                    baos = new ByteArrayOutputStream();
                    molExporter = new MolExporter(baos, "inchikey");
                    molExporter.write(molecule);
                    String inchiKey = baos.toString().trim().split("=")[1];
                    for (int i = 2; i < header.length; i++) {
                        insertPropertyStatement.setString(1, header[i]);
                        insertPropertyStatement.setString(2, inchiKey);
                        insertPropertyStatement.setDouble(3, Double.parseDouble(tokens[i]));
                        insertPropertyStatement.addBatch();
                    }
                }
            }
            insertPropertyStatement.executeBatch();

        } catch (SQLException ex) {
            Logger.getLogger(PopulateDescriptors.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PopulateDescriptors.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(PopulateDescriptors.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 0;
    }

    public static void main(String... args) throws DbException {
        String sdFile = "/home/chung/Desktop/first.sdf";
        String descriptorsFile = "/home/chung/Desktop/descr3.txt";
        PopulateDescriptors pd = new PopulateDescriptors(sdFile, descriptorsFile);
        pd.write();

    }
}
