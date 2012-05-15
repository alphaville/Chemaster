package org.chemaster;

import java.io.FileInputStream;
import java.util.BitSet;
import java.util.List;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.fingerprint.EStateFingerprinter;
import org.openscience.cdk.fingerprint.ExtendedFingerprinter;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.fingerprint.FingerprinterTool;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * For testing purposes solely
 * @author chung
 */
public class Chemaster {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String naphthaleneFile = "naphthalene.sdf";
        String muphthaleneltiFile = "multiphtalene.sdf";

        FileInputStream naphthaleneIS = new FileInputStream(naphthaleneFile);
        FileInputStream multiphthaleneIS = new FileInputStream(muphthaleneltiFile);

        MDLReader naphthaleneReader = new MDLReader(naphthaleneIS);
        MDLReader multiphthaleneReader = new MDLReader(multiphthaleneIS);

        ChemFile naphthleneFile = (ChemFile) naphthaleneReader.read((ChemObject) new ChemFile());
        ChemFile multiphthleneFile = (ChemFile) multiphthaleneReader.read((ChemObject) new ChemFile());

        List<IAtomContainer> napthaleneList = ChemFileManipulator.getAllAtomContainers(naphthleneFile);
        List<IAtomContainer> multiphthaleneList = ChemFileManipulator.getAllAtomContainers(multiphthleneFile);

        IAtomContainer naphtalene = napthaleneList.get(0);
        IAtomContainer multiphthalene = multiphthaleneList.get(0);

        
        EStateFingerprinter ifp2 = new EStateFingerprinter();        
        System.out.println(ifp2.getFingerprint(naphtalene));
        System.out.println(ifp2.getFingerprint(multiphthalene));
        
        SmilesGenerator smiGen = new SmilesGenerator(true);        
        System.out.println(smiGen.createSMILES(naphtalene));
        
        System.out.println(InChIGeneratorFactory.getInstance().getInChIGenerator(naphtalene).getInchi());
        
        
    }
}
