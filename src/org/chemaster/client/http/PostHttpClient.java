package org.chemaster.client.http;

import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import com.hp.hpl.jena.ontology.OntModel;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.chemaster.client.IPostClient;
import org.chemaster.client.ServiceInvocationException;
import org.chemaster.client.collection.Media;
import org.chemaster.client.collection.RequestHeaders;

/**
 * A client used to perform POST operations. It is used to perform POST requests in
 * a configurable way allowing users to specify the POSTed object and the various
 * header parameters.
 * 
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class PostHttpClient extends AbstractHttpClient implements IPostClient {

    /** Type of the posted content*/
    private String contentType = null;
    /** Parameters to be posted as application/x-www-form-urlencoded (if any) */
    private Map<String, List<String>> postParameters = new LinkedHashMap<String, List<String>>();
    private OntModel model;
    /** Arbitrary object to be posted to the remote server s*/
    private File fileContentToPost = null;
    /** A simple string to be posted to the remote service */
    private String stringToPost;
    private String bytesToPost;
    /** A StAX component that implements the interface {@link IStAXWritable }
    that will be posted to the remote server via the method {@link IStAXWritable#writeRdf(java.io.OutputStream)
    write(OutputStream)} that writes the component to an outputstream pointing to the
    remote stream
     */
    public WriteLock postLock = new ReentrantReadWriteLock().writeLock();

    public PostHttpClient() {
        super();
    }

    public PostHttpClient(URI uri) {
        super();
        this.uri = uri;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public PostHttpClient setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public PostHttpClient setContentType(Media media) {
        this.contentType = media.getMime();
        return this;
    }

    /**
     * Set a file whose contents are to be posted to the remote server specified
     * in the constructor of this class. If the file is not found under the specified
     * path, an IllegalArgumentException is thrown. Because the type of the file is
     * in general unknown and it is not considered to be a good practise to deduce the
     * file type from the file extension, it is up to the user to specify the content
     * type of the posted object using the method {@link PostHttpClient#setContentType(java.lang.String)
     * setContentType}. Since it is not possible to POST entities of different content
     * types to an HTTP server, any invokation to this method will override any previous
     * invokation of {@link PostHttpClient#setPostable(com.hp.hpl.jena.ontology.OntModel)
     * setPostable(OntModel) } and {@link PostHttpClient#setPostable(java.lang.String, boolean) 
     * setPostable(String)}.
     *
     * @param objectToPost
     *      File whose contents are to be posted.
     * @return
     *      This post client
     * @throws IllegalArgumentException
     *      In case the provided file does not exist
     */
    @Override
    public PostHttpClient setPostable(File objectToPost) {
        if (objectToPost != null && !objectToPost.exists()) {
            throw new IllegalArgumentException(new FileNotFoundException("No file was found at the specified path!"));
        }
        this.fileContentToPost = objectToPost;
        return this;
    }

    @Override
    public PostHttpClient setPostable(String string, boolean binary) {
        if (binary) {
            this.bytesToPost = string;
        } else {
            this.stringToPost = string;
        }
        return this;
    }

    /**
     * Add a parameter which will be posted to the target URI. Once the parameter is
     * submitted to the PostHttpClient, it is stored as URL-encoded using the UTF-8 encoding.
     * @param paramName Parameter name
     * @param paramValue Parameter value
     * @return This object
     * @throws NullPointerException If paramName is <code>null</code>.
     */
    @Override
    public PostHttpClient addPostParameter(String paramName, String paramValue) throws NullPointerException {
        if (paramName == null) {
            throw new NullPointerException("paramName must be not null");
        }
        try {
            String encodedParamName = URLEncoder.encode(paramName, URL_ENCODING);
            String encodedParamvalue = paramValue != null ? URLEncoder.encode(paramValue, URL_ENCODING) : "";
            List<String> list = postParameters.get(encodedParamName); // check if param exists
            if (list != null) { // param exists
                list.add(encodedParamvalue);
            } else { // param does not exist
                list = new ArrayList<String>();
                list.add(encodedParamvalue);
                postParameters.put(encodedParamName, list);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public AbstractHttpClient addHeaderParameter(String paramName, String paramValue) throws NullPointerException, IllegalArgumentException {
        if (paramName == null) {
            throw new NullPointerException("ParamName is null");
        }
        if (paramValue == null) {
            throw new NullPointerException("ParamValue is null");
        }
        if (RequestHeaders.ACCEPT.equalsIgnoreCase(paramName)) {
            setMediaType(paramValue);
            return this;
        }
        if (RequestHeaders.CONTENT_TYPE.equalsIgnoreCase(paramName)) {
            setContentType(paramValue);
            return this;
        }
        return super.addHeaderParameter(paramName, paramValue);
    }

    private String getParametersAsQuery() {
        if (postParameters.isEmpty()) {
            return "";
        }
        StringBuilder string = new StringBuilder();
        final int nParams = postParameters.size();

        if (nParams > 0) {
            for (Map.Entry<String, List<String>> e : postParameters.entrySet()) {
                List<String> values = e.getValue();
                for (String value : values) {
                    string.append(e.getKey());
                    string.append("=");
                    if (e.getValue() != null) {
                        string.append(value);
                    }
                    string.append("&");
                }
            }
        }
        string.deleteCharAt(string.length() - 1);
        return new String(string);
    }

    /** Initialize a connection to the target URI */
    @Override
    protected java.net.HttpURLConnection initializeConnection(final java.net.URI uri) throws ServiceInvocationException {
        try {
            java.net.HttpURLConnection.setFollowRedirects(true);
            java.net.URL target = uri.toURL();
            con = (java.net.HttpURLConnection) target.openConnection();
            con.setRequestMethod(METHOD);
            con.setAllowUserInteraction(false);
            con.setDoInput(true);
            con.setDoOutput(true); // allow data to be posted
            con.setUseCaches(false);
            if (contentType != null) {
                con.setRequestProperty(RequestHeaders.CONTENT_TYPE, contentType);
            }
            if (acceptMediaType != null) {
                con.setRequestProperty(RequestHeaders.ACCEPT, acceptMediaType);
            }
            if (!headerValues.isEmpty()) {
                for (Map.Entry<String, String> e : headerValues.entrySet()) {
                    con.setRequestProperty(e.getKey(), e.getValue());// These are already URI-encoded!
                }
            }
            /* If there are some parameters to be posted, then the POST will
             * declare the posted data as application/x-form-urlencoded.
             */
            if (!postParameters.isEmpty()) {
                setContentType(Media.APPLICATION_FORM_URL_ENCODED);
                con.setRequestProperty(RequestHeaders.CONTENT_TYPE, contentType);
                con.setRequestProperty(RequestHeaders.CONTENT_LENGTH,
                        Integer.toString(getParametersAsQuery().getBytes().length));
            }
            return con;
        } catch (final IOException ex) {
            throw new ServiceInvocationException("Unable to connect to the remote service at '" + getUri() + "'", ex);
        } catch (final Exception unexpectedException) {
            throw new ServiceInvocationException("Unexpected condition while attempting to "
                    + "establish a connection to '" + uri + "'", unexpectedException);
        }
    }

    /**
     * According to the the configuration of the PostHttpClient, permorms a remote POST
     * request to the server identified by the URI provided in the contructor. First,
     * the protected method {@link PostHttpClient#initializeConnection(java.net.URI)
     * initializeConnection(URI)} is invoked and then a DataOutputStream opens to
     * tranfer the data to the server.
     *
     * @throws ToxOtisException
     *      Encapsulates an IOException which might be thrown due to I/O errors
     *      during the data transaction.
     */
    @Override
    public void post() throws ServiceInvocationException {
        connect(uri);
        DataOutputStream wr;
        try {
            getPostLock().lock(); // LOCK
            wr = new DataOutputStream(con.getOutputStream());
            String query = getParametersAsQuery();
            if (query != null && !query.isEmpty()) {
                wr.writeBytes(getParametersAsQuery());// POST the parameters
            }
            if (model != null) {
                model.write(wr);
            }
            if (stringToPost != null) {
                wr.writeChars(stringToPost);
            }
            if (bytesToPost != null) {
                wr.writeBytes(bytesToPost);
            }
            if (fileContentToPost != null) {
                FileReader fr = null;
                BufferedReader br = null;
                try {
                    fr = new FileReader(fileContentToPost);
                    br = new BufferedReader(fr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        wr.writeBytes(line);
                        wr.writeChars("\n");
                    }
                } catch (IOException ex) {
                    throw new ServiceInvocationException("Unable to post data to the remote service at '" + getUri() + "' - The connection dropped "
                            + "unexpectidly while POSTing.", ex);
                } finally {
                    Throwable thr = null;
                    if (br != null) {
                        try {
                            br.close();
                        } catch (final IOException ex) {
                            thr = ex;
                        }
                    }
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (final IOException ex) {
                            thr = ex;
                        }
                    }
                    if (thr != null) {
                        ServiceInvocationException connExc = new ServiceInvocationException("Stream could not close", thr);
                    }
                }
            }
            wr.flush();
            wr.close();
        } catch (final IOException ex) {
            ServiceInvocationException postException = new ServiceInvocationException("Exception caught while posting the parameters to the "
                    + "remote web service located at '" + getUri() + "'", ex);
            throw postException;
        } finally {
            getPostLock().unlock(); // UNLOCK
        }
    }

    @Override
    public WriteLock getPostLock() {
        return postLock;
    }
}
