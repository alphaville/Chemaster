package org.chemaster.pubchem;

import org.chemaster.client.ServiceInvocationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class InchiKeyToIUPACNameTest {
    
    public InchiKeyToIUPACNameTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() throws ServiceInvocationException {
        InchiKeyToIUPACName o = new InchiKeyToIUPACName("OMFXVFTZEKFJBZ-UHFFFAOYSA-N");
        String cid = o.getCid();
        String iupacName = o.getIupacName();
        System.out.println("Molecule with CID: "+cid+" has name "+iupacName);
        
        
    }
}
