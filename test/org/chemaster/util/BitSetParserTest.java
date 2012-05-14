package org.chemaster.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Pantelis Sopasakis
 */
public class BitSetParserTest {

    public BitSetParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        String bs = "{62, 95, 161, 213, 253, 463, 535, 574, 742}";
        BitSetParser parser = new BitSetParser(bs);
        assertEquals(bs, parser.parse().toString());
    }

    @Test
    public void testSomeMethod2() {
        String bs = "{6}";
        BitSetParser parser = new BitSetParser(bs);
        assertEquals(bs, parser.parse().toString());
    }
    
    @Test
    public void testSomeMethod3() {
        String bs = "{}";
        BitSetParser parser = new BitSetParser(bs);
        assertEquals(bs, parser.parse().toString());
    }
    
    @Test
    public void testSomeMethod4() {
        String bs = "{5, 25, 26, 28, 30, 38, 75, 76, 83, 84, 85, 87, 127, 128, 138, 148, 206, 238, 300, 336, 392, 426, 428, 443, 470, 474, 480, 522, 534, 542, 549, 556, 588, 621, 637, 644, 660, 669, 741, 742, 743, 747, 752, 756, 758, 760, 784, 798, 830, 846, 850, 863, 865, 866, 906, 920, 922, 924, 929, 943, 949, 976, 1014}";
        BitSetParser parser = new BitSetParser(bs);
        assertEquals(bs, parser.parse().toString());
    }
}
