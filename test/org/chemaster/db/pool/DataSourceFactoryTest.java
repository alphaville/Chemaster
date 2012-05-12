/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chemaster.db.pool;

import java.sql.Connection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class DataSourceFactoryTest {

    public DataSourceFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        assertTrue(DataSourceFactory.getInstance().ping(10));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        DataSourceFactory.getInstance().close();
    }

    @Test
    public void testInvokeDataSource() {
        try {
            DataSourceFactory factory = DataSourceFactory.getInstance();

            Connection connection = factory.getDataSource().getConnection();
            assertNotNull(connection);
            assertTrue(factory.ping(50));
        } catch (Exception ex) {
            fail("Database is inaccessible! " + ex);
        }
    }
}
