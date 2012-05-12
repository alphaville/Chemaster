package org.chemaster.client.http;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.chemaster.client.IGetClient;
import org.chemaster.client.ServiceInvocationException;
import org.chemaster.client.collection.RequestHeaders;

/**
 * A client that performs GET requests.
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class GetHttpClient extends AbstractHttpClient implements IGetClient {

    /** Create a new instance of GetHttpClient */
    public GetHttpClient() {
    }

    public GetHttpClient(final URI uri) {
        setUri(uri);
    }

    @Override
    protected java.net.HttpURLConnection initializeConnection(final java.net.URI uri) throws ServiceInvocationException {
        if (uri == null) {
            throw new NullPointerException("Null Pointer while initializing connection (in GetHttpClient). The input "
                    + "argument 'uri' to the method GetHttpClient#initializeConnection(java.net.URI)::java.net.HttpURLConnection "
                    + "should not be null.");
        }
        try {
            java.net.HttpURLConnection.setFollowRedirects(true);
            java.net.URL url = uri.toURL();
            con = (java.net.HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestMethod(METHOD);
            if (acceptMediaType != null) {
                con.setRequestProperty(RequestHeaders.ACCEPT, acceptMediaType);
            }
            if (headerValues != null && !headerValues.isEmpty()) {
                for (Map.Entry<String, String> e : headerValues.entrySet()) {
                    con.setRequestProperty(e.getKey(), e.getValue());// These are already URI-encoded!
                }
            }
            return con;
        } catch (final IOException ex) {
            throw new ServiceInvocationException("Unable to connect to the remote service at '" + getUri() + "'", ex);
        } catch (final Exception unexpectedException) {
            throw new ServiceInvocationException("Unexpected condition while attempting to "
                    + "establish a connection to '" + uri + "'", unexpectedException);
        }
    }
}

