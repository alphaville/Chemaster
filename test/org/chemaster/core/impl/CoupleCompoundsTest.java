package org.chemaster.core.impl;

import org.chemaster.core.ICompound;
import org.chemaster.core.ICoupleCompounds;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class CoupleCompoundsTest {

    public CoupleCompoundsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        ICompound c1 = new Compound("AFVMDRFUJONSCM-UHFFFAOYSA-N"),
                c2 = new Compound("OMFXVFTZEKFJBZ-IQDVQEQVSA-N");
        ICoupleCompounds couple = new CoupleCompounds(c1, c2);
        String hash1 = (couple.hashKey());
        couple = new CoupleCompounds(c2, c1);
        assertEquals(hash1, couple.hashKey());
    }
}
