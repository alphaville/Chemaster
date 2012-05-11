package org.chemaster;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.calculations.ElementalAnalyserPlugin;
import chemaxon.struc.Molecule;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author chung
 */
public class Chemaster {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        FileInputStream fileInputStream = new FileInputStream("/home/chung/Desktop/first.sdf");
        FileOutputStream os = new FileOutputStream("out.sdf");
        MolImporter molImporter = new MolImporter(fileInputStream);
        MolExporter molExporter = new MolExporter(os, "inchikey");

        ElementalAnalyserPlugin plugin = new ElementalAnalyserPlugin();
        plugin.setDoublePrecision(2);

        Molecule molecule;
        while ((molecule = molImporter.read()) != null) {
            molExporter.write(molecule);
            plugin.setMolecule(molecule);
            plugin.run();
            double mass = plugin.getMass();
            double exactMass = plugin.getExactMass();
            System.out.println(mass);
            System.out.println(exactMass);
            String composition = plugin.getComposition();
            System.out.println(composition);
            
        }

        molImporter.close();
        molExporter.close();
    }
}
