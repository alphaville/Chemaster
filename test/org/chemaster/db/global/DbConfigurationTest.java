package org.chemaster.db.global;

import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class DbConfigurationTest {
    
    public DbConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        Properties porps = DbConfiguration.getInstance().getProperpties();
        assertEquals("1000",porps.getProperty("c3p0.maxPoolSize"));
    }
}
