package org.chemaster.core;

import org.openscience.cdk.exception.CDKException;

/**
 *
 * @author chung
 */
public interface ICoupleCompounds {
    
    ICoupleCompounds setCompounds(ICompound compound1, ICompound compound2);
    
    double getSimilarity() throws CDKException;
    
    String hashKey();
}
