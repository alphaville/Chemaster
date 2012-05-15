package org.chemaster.core.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import org.chemaster.core.ICompound;
import org.chemaster.core.ICoupleCompounds;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.similarity.Tanimoto;

/**
 *
 * @author chung
 */
public class CoupleCompounds implements ICoupleCompounds {

    ICompound compound1;
    ICompound compound2;
    Double similarity = null;
    Double ext_similarity = null;
    Double estate_similarity = null;

    public CoupleCompounds(ICompound compound1, ICompound compound2) {
        this.compound1 = compound1;
        this.compound2 = compound2;
    }

    public CoupleCompounds() {
    }

    @Override
    public ICoupleCompounds setCompounds(ICompound compound1, ICompound compound2) {
        this.compound1 = compound1;
        this.compound2 = compound2;
        return this;
    }

    @Override
    public double getSimilarity() throws CDKException {
        if (similarity == null) {
            similarity = new Double(Tanimoto.calculate(compound1.getFingerprint(), compound2.getFingerprint()));
        }
        return similarity;
    }

    @Override
    public double getExtendedSimilarity() throws CDKException {
        if (ext_similarity == null) {
            ext_similarity = new Double(Tanimoto.calculate(compound1.getExtFingerprint(), compound2.getExtFingerprint()));
        }
        return ext_similarity;
    }

    @Override
    public double getESTATESimilarity() throws CDKException {
        if (estate_similarity == null) {
            estate_similarity = new Double(Tanimoto.calculate(
                    compound1.getESTATEFingerprint(), compound2.getESTATEFingerprint()));
        }
        return estate_similarity;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CoupleCompounds other = (CoupleCompounds) obj;
        return true;
    }

    @Override
    public String hashKey() {
        String digestAsString = null;
        if (compound1 == null || (compound1 != null && compound1.getInchiKey() == null)) {
            throw new NullPointerException("NPE for compound 1");
        }
        if (compound2 == null || (compound2 != null && compound2.getInchiKey() == null)) {
            throw new NullPointerException("NPE for compound 2");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            List<String> compoundKeys = new ArrayList<String>();
            compoundKeys.add(compound1.getInchiKey());
            compoundKeys.add(compound2.getInchiKey());
            Collections.sort(compoundKeys, String.CASE_INSENSITIVE_ORDER);
            StringBuilder sb = new StringBuilder();
            for (String s : compoundKeys) {
                sb.append(s);
            }
            md.reset();
            md.update(sb.toString().getBytes("UTF-8"));
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            digestAsString = bigInt.toString(16);
            while (digestAsString.length() < 32) {
                digestAsString = "0" + digestAsString;
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return digestAsString;
    }

    @Override
    public ICompound[] getCompounds() {
        return new ICompound[]{compound1, compound2};
    }
}
