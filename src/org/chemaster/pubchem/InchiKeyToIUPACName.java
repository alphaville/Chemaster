package org.chemaster.pubchem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.chemaster.client.ClientFactory;
import org.chemaster.client.IGetClient;
import org.chemaster.client.ServiceInvocationException;
import org.chemaster.client.collection.Media;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Pantelis Sopasakis
 */
public class InchiKeyToIUPACName {

    private final String inchiKey;
    private static final String __IUPAC_FROM_INCHIKEY__ =
            "http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/%s/property/IUPACName/";
    private String cid;
    private String iupacName;
    Document dom;

    public InchiKeyToIUPACName(String inchiKey) throws ServiceInvocationException {
        this.inchiKey = inchiKey;
        fetchFromRemote();
    }

    public String getCid() {
        return cid;
    }

    public String getIupacName() {
        return iupacName;
    }

    private void fetchFromRemote() throws ServiceInvocationException {
        IGetClient cli = null;
        try {
            cli = ClientFactory.createGetClient(
                    new URI(String.format(__IUPAC_FROM_INCHIKEY__, inchiKey)));
            cli.setMediaType(Media.APPLICATION_XML);
        } catch (URISyntaxException ex) {
            Logger.getLogger(InchiKeyToIUPACName.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(cli.getRemoteStream());
            Element docEle = dom.getDocumentElement();
            NodeList root = docEle.getElementsByTagName("Properties");
            if (root != null && root.getLength() > 0) {
                Element ele = (Element) root.item(0);
                NodeList list = ele.getElementsByTagName("IUPACName");
                iupacName = list.item(0).getTextContent();
                list = ele.getElementsByTagName("CID");
                cid = list.item(0).getTextContent();
            }

        } catch (final ParserConfigurationException pce) {
            Logger.getLogger(InchiKeyToIUPACName.class.getName()).log(Level.SEVERE, null, pce);
        } catch (final SAXException se) {
            Logger.getLogger(InchiKeyToIUPACName.class.getName()).log(Level.SEVERE, null, se);
        } catch (final IOException ioe) {
            Logger.getLogger(InchiKeyToIUPACName.class.getName()).log(Level.SEVERE, null, ioe);
        }finally{
            if (cli!=null){
                try {
                    cli.close();
                } catch (final IOException ex) {
                    Logger.getLogger(InchiKeyToIUPACName.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
