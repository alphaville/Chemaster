package org.chemaster;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.XYZWriter;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.APolDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.AcidicGroupCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.AutocorrelationDescriptorPolarizability;
import org.openscience.cdk.qsar.descriptors.molecular.CPSADescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.FragmentComplexityDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HybridizationRatioDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.MDEDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.TPSADescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.WHIMDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.WienerNumbersDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.ZagrebIndexDescriptor;
import org.openscience.cdk.similarity.Tanimoto;
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
        String filename = "first.sdf";

        FileInputStream fileInputStream = new FileInputStream(filename);
        MDLReader reader = new MDLReader(fileInputStream);
        ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);


        Map<Object, Object> m = containersList.get(0).getProperties();
        Set<Entry<Object, Object>> set = m.entrySet();
        Iterator<Entry<Object, Object>> iter = set.iterator();
        while (iter.hasNext()) {
            Entry<Object, Object> e = iter.next();
            System.out.println(e.getKey() + " > " + e.getValue());
        }
        Object name = containersList.get(0).getProperty("cdk:Title");
        System.out.println("name :: " + name);

        InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(containersList.get(1));
        System.out.println("inchiKey :: " + gen.getInchiKey());
        System.out.println("inchi :: " + gen.getInchi());
        
        SmilesGenerator smi = new SmilesGenerator();
        String smiles = smi.createSMILES(containersList.get(3));
        System.out.println(smiles);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XYZWriter xyz = new XYZWriter(baos);
        xyz.write(containersList.get(0));
        xyz.close();
        System.out.println(baos);
        BitSet fingerprint1 = new Fingerprinter().getFingerprint(containersList.get(0));
        BitSet fingerprint2 = new Fingerprinter().getFingerprint(containersList.get(1));
        
        IMolecularDescriptor desc = new ZagrebIndexDescriptor();
        IMolecularDescriptor d = new HybridizationRatioDescriptor();
        
        System.out.println("Desc : "+d.calculate(containersList.get(2)).getValue());
        System.out.println("Desc : "+desc.calculate(containersList.get(2)).getValue());

        double t1 = Tanimoto.calculate(fingerprint1, fingerprint2);
        System.out.println("similarity = "+t1);
    }
}
