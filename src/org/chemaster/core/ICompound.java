package org.chemaster.core;

import java.util.BitSet;
import java.util.Map;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 *
 * @author chung
 */
public interface ICompound {
    
    ICompound setInchiKey(String inchiKey);
    
    String getInchiKey();
    
    ICompound setFingerprint(BitSet bs);
    
    BitSet getFingerprint();
    
    Map<String, String> getRepresentations();
    
    String getRepresentation(String representation_type);
    
    IAtomContainer asContainer();
    
}
