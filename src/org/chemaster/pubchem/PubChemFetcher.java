package org.chemaster.pubchem;

import java.io.InputStreamReader;
import java.net.URI;
import org.chemaster.client.ClientFactory;
import org.chemaster.client.IGetClient;
import org.chemaster.client.collection.Media;
import org.chemaster.db.tools.MolWriter;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

/**
 *
 * @author chung
 */
public class PubChemFetcher {

    int from = 1;
    int to = 10;
    private static final String __urlPattern_ = "http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/%s";

    public PubChemFetcher() {
    }

    public PubChemFetcher(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public void fetch() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(i);
            sb.append(",");
        }
        sb.append(to);

        URI uri = new URI(String.format(__urlPattern_, sb));
        System.out.println(uri);
        IGetClient cli = ClientFactory.createGetClient(uri);
        cli.setMediaType(Media.CHEMICAL_MDLSDF);

        IChemObjectBuilder ob = DefaultChemObjectBuilder.getInstance();
        IteratingMDLReader rdr = new IteratingMDLReader(new InputStreamReader(cli.getRemoteStream()), ob);
        MolWriter mw = new MolWriter(rdr);
        mw.setSourceUri(uri);
        mw.write();
        
        mw.close();
        rdr.close();
        cli.close();

    }

    public static void main(String... arts) throws Exception {
        int offset = 1;
        int batch = 5;
        int howMany= 1000;
        int ceiling = offset + howMany;
        for (int i = 0; i < ceiling; i = i + batch) {
            int from = i;
            int to = i + batch - 1;
            from += offset;
            to += offset;
            PubChemFetcher f = new PubChemFetcher(from, to);
            f.fetch();
        }
    }
    
}
