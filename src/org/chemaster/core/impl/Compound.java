package org.chemaster.core.impl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chemaster.core.ICompound;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 *
 * @author chung
 */
public class Compound implements ICompound {

    private Map<String, String> REPRESENTATION_MAP = new HashMap<String, String>();
    private String inchiKey = null;
    private BitSet bs;

    public Compound() {
    }

    public Compound(String inchiKey) {
        this.inchiKey = inchiKey;
    }

    @Override
    public ICompound setInchiKey(String inchiKey) {
        this.inchiKey = inchiKey;
        return this;
    }

    @Override
    public String getInchiKey() {
        return inchiKey;
    }

    @Override
    public Map<String, String> getRepresentations() {
        return REPRESENTATION_MAP;
    }

    @Override
    public String getRepresentation(String representation_type) {
        return REPRESENTATION_MAP.get(representation_type);
    }

    @Override
    public ICompound setFingerprint(BitSet bs) {
        this.bs = bs;
        return this;
    }

    @Override
    public BitSet getFingerprint() {
        return this.bs;
    }

    @Override
    public IAtomContainer asContainer() {
        try {
            String sdf = getRepresentation("sdf");
            ByteArrayInputStream bais = null;
            bais = new ByteArrayInputStream(sdf.getBytes("UTF-8"));
            MDLReader reader = new MDLReader(bais);
            ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
            List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
            return containersList.get(0);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Compound.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CDKException ex) {
            Logger.getLogger(Compound.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Compound other = (Compound) obj;
        if ((this.inchiKey == null) ? (other.inchiKey != null) : !this.inchiKey.equals(other.inchiKey)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.inchiKey != null ? this.inchiKey.hashCode() : 0);
        return hash;
    }
}
