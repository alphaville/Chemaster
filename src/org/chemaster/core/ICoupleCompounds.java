package org.chemaster.core;

import org.openscience.cdk.exception.CDKException;

/**
 *
 * @author chung
 */
public interface ICoupleCompounds {

    ICompound[] getCompounds();

    ICoupleCompounds setCompounds(ICompound compound1, ICompound compound2);

    double getSimilarity() throws CDKException;

    double getExtendedSimilarity() throws CDKException;

    double getESTATESimilarity() throws CDKException;

    String hashKey();
}
