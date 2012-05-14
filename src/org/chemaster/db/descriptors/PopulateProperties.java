package org.chemaster.db.descriptors;

import java.io.BufferedReader;
import java.io.File;
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

/*
 * Reads the file dList.txt and registers all descriptors in 
 * the database. The file dList.txt should be formatted as explained
 * in the documentation.
 * 
 * @author Pantelis Sopasakis
 */
public class PopulateProperties extends DbWriter {

    private final String dListPath;
    private static final String insertProperty =
            "INSERT IGNORE INTO `Property` (`name`,`type`,`description`,`isExperimental`,`tag`) "
            + "VALUES (?,?,?,false,?)";
    private PreparedStatement insertPropertyStatement = null;

    public PopulateProperties(String dListPath) {
        super();
        this.dListPath = dListPath;
    }

    @Override
    public int write() throws DbException {
        Connection connection = getConnection();
        {
            try {
                connection.setAutoCommit(false);
                insertPropertyStatement = connection.prepareStatement(insertProperty);
                File f = new File(dListPath);
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String strLine = null;
                while ((strLine = br.readLine()) != null) {
                    String[] tokens = strLine.split("\t");
                    String name = tokens[1];
                    String description = tokens[2];
                    String block = tokens[3];
                    String subblock = tokens[4];
                    if (!"Name".equals(name)) {
                        insertPropertyStatement.setString(1, name);
                        insertPropertyStatement.setString(2, "");
                        insertPropertyStatement.setString(3, description);
                        insertPropertyStatement.setString(4, block + "::" + subblock);
                        insertPropertyStatement.addBatch();
                    }
                }
                insertPropertyStatement.executeBatch();
                connection.commit();
            } catch (IOException ex) {
                Logger.getLogger(PopulateProperties.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(PopulateProperties.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }
        }
        return 0;
    }

    public static void main(String... args) throws DbException {
        PopulateProperties p = new PopulateProperties("dList.txt");
        p.write();
        p.close();
        DataSourceFactory.getInstance().close();
    }
}
