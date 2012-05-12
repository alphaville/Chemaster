package org.chemaster.client.http;

import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.chemaster.client.*;
import org.chemaster.client.collection.Media;
import org.chemaster.client.collection.RequestHeaders;

/**
 * An abstract class providing necessary methods for the implementation of a
 * HTTP client. 
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public abstract class AbstractHttpClient implements IClient {

    /** Target URI */
    protected URI uri = null;
    /** Connection to the above URI */
    protected java.net.HttpURLConnection con = null;
    /** Size of a buffer used to download the data from the remote server */
    protected static final int bufferSize = 4194304;
    /** Accepted mediatype  */
    protected String acceptMediaType = null;
    /** A mapping from parameter names to their corresponding values */
    protected Map<String, String> headerValues = new HashMap<String, String>();
    private ReentrantReadWriteLock.ReadLock readLock = new ReentrantReadWriteLock().readLock();
    private ReentrantReadWriteLock.WriteLock connectionLock = new ReentrantReadWriteLock().writeLock();
    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractHttpClient.class);

    @Override
    public WriteLock getConnectionLock() {
        return connectionLock;
    }

    @Override
    public ReadLock getReadLock() {
        return readLock;
    }

    /**
     * Get the targetted URI
     * @return The target URI
     */
    @Override
    public URI getUri() {
        return uri;
    }

    /**
     * Retrieve the specified media type which is the value for the <code>Accept</code>
     * HTTP Header.
     * @return
     *      The accepted media type.
     */
    @Override
    public String getMediaType() {
        return acceptMediaType;
    }

    /**
     * Note: if the parameter name (paramName) is either 'Accept' or 'Content-type', this
     * method will override {@link IClient#setMediaType(java.lang.String) setMediaType} and
     * {@link IPostClient#setContentType(java.lang.String) setContentType} respectively. In general
     * it is not advisable that you choose this method for setting values to these headers. Once the
     * parameter name and its value are submitted to the client, they are encoded using the
     * standard UTF-8 encoding.
     * @param paramName Name of the parameter which will be posted in the header
     * @param paramValue Parameter value
     * @return This object
     * @throws NullPointerException
     *          If any of the arguments is null
     */
    @Override
    public AbstractHttpClient addHeaderParameter(String paramName, String paramValue) throws NullPointerException {
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
        headerValues.put(paramName, paramValue);
        return this;
    }

    /**
     * Initiate a connection to the remote location identified by the provided URI and
     * using the already specified header parameters.
     * 
     * @param uri
     *      The location to which the HTTP connection should be made.
     * @return
     *      An instance of HttpURLConnection that is used to perform the remote
     *      HTTP request.
     * @throws ToxOtisException
     *      In case an error status code is received from the remote service or
     *      an I/O exception is thrown due to communication problems with the remote
     *      server.
     */
    protected abstract java.net.HttpURLConnection initializeConnection(final java.net.URI uri) throws ServiceInvocationException;

    protected java.net.HttpURLConnection connect(final java.net.URI uri) throws ServiceInvocationException {
        connectionLock.lock();
        try {
            return initializeConnection(uri);
        } finally {
            connectionLock.unlock();
        }
    }

    protected InputStream getConnectionInputStream() throws ServiceInvocationException {
        getReadLock().lock();
        try {
            if (con == null) {
                throw new NullPointerException("No connection established");
            }
            InputStream is = null;
            try {
                is = con.getInputStream();
            } catch (IOException ex) {
                ServiceInvocationException connectionExc = new ServiceInvocationException("Input-Output error occured while connecting to "
                        + "the server. Cannot initialize an InputStream to " + getUri(), ex);
                throw connectionExc;
            }
            return is;
        } finally {
            getReadLock().unlock();
        }

    }

    /**
     * Get the body of the HTTP response as InputStream.
     * @return
     *      InputStream for the remote HTTP response
     * @throws ServiceInvocationException
     *      In case an error status code is received from the remote location.
     * @throws ConnectionException
     *      In case no connection is feasible to the remote resource. Stream cannot
     *      open and the generated connection is <code>null</code>
     * @throws java.io.IOException
     *      In case some communication error occurs during the transmission
     *      of the data.
     */
    @Override
    public java.io.InputStream getRemoteStream() throws ServiceInvocationException {
        getReadLock().lock();
        try {
            if (con == null) {
                con = connect(getUri());
            }
            if (con == null) {
                ServiceInvocationException badConnection =
                        new ServiceInvocationException("Cannot establish connection to " + getUri());
                throw badConnection;
            }
            int connectionResponseCode = getResponseCode();
            if (connectionResponseCode == 200 || connectionResponseCode == 202 || connectionResponseCode == 201) {
                return new java.io.BufferedInputStream(getConnectionInputStream(), bufferSize);
            } else {
                return new java.io.BufferedInputStream(con.getErrorStream(), bufferSize);
            }
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Get the response body as a String in the format specified in the Accept header
     * of the request.
     *
     * @return
     *      String consisting of the response body (in a MediaType which results
     *      from content negotiation, taking into account the Accept header of the
     *      request)
     * @throws ToxOtisException
     *      In case some communication, server or request error occurs.
     */
    @Override
    public String getResponseText() throws ServiceInvocationException {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            if (con == null) {
                con = connect(uri);
            }
            is = getRemoteStream();
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new String(sb);
        } catch (IOException io) {
            ServiceInvocationException connectionExc = new ServiceInvocationException("Input-Output error occured while connecting to "
                    + "the server. Cannot read input stream to " + getUri(), io);
            throw connectionExc;
        } finally {
            IOException closeException = null;
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    closeException = ex;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    closeException = ex;
                }
            }
            if (closeException != null) {
                ServiceInvocationException connExc =
                        new ServiceInvocationException("Stream could not close", closeException);
                throw connExc;
            }
        }
    }

    /**
     * Get the HTTP status of the response
     * @return
     *      Response status code.
     * @throws ToxOtisException
     *      In case the connection cannot be established because a {@link ToxOtisException }
     *      is thrown from the method {@link AbstractHttpClient#initializeConnection(java.net.URI)
     *      initializeConnection(URI)}.
     * @throws java.io.IOException
     *      In case some communication error with the remote location occurs during
     *      the transaction of data.
     */
    @Override
    public int getResponseCode() throws ServiceInvocationException {
        int responseCode = 0;
        try {
            if (con == null) {
                connect(getUri());
            }
            responseCode = con.getResponseCode();
        } catch (IOException ex) {
            ServiceInvocationException connectionExc = new ServiceInvocationException("Input-Output error occured while connecting to "
                    + "the server at " + getUri(), ex);
            throw connectionExc;
        }
        return responseCode;
    }

    /**
     * Specify the mediatype to be used in the <tt>Accept</tt> header.
     * @param mediaType 
     *      Accepted mediatype
     *
     * @see RequestHeaders#ACCEPT
     */
    @Override
    public AbstractHttpClient setMediaType(String mediaType) {
        this.acceptMediaType = mediaType;
        return this;
    }

    /**
     * Specify the mediatype to be used in the <tt>Accept</tt> header providing
     * an instance of {@link Media }.
     * @param mediaType
     *      Accepted mediatype
     * @see RequestHeaders#ACCEPT
     */
    @Override
    public AbstractHttpClient setMediaType(Media mediaType) {
        this.acceptMediaType = mediaType.getMime();
        return this;
    }

    /**
     * Set the URI on which the GET method is applied.
     * @param uri
     *      The URI that will be used by the client to perform the remote connection.
     */
    @Override
    public AbstractHttpClient setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Provide the target URI as a String
     * @param uri The target URI as a String.
     * @throws java.net.URISyntaxException In case the provided URI is syntactically
     * incorrect.
     */
    @Override
    public AbstractHttpClient setUri(String uri) throws java.net.URISyntaxException {
        this.uri = new URI(uri);
        return this;
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if (con != null) {
            con.disconnect();
        }
    }

    /**
     * Get the response of the remote service as a Set of URIs. The media type of
     * the request, as specified by the <code>Accept</code> header is set to
     * <code>text/uri-list</code>.
     * @return
     *      Set of URIs returned by the remote service.
     * @throws ToxOtisException
     *      In case some I/O communication error inhibits the transimittance of
     *      data between the client and the server or a some stream cannot close.
     */
    @Override
    public java.util.Set<URI> getResponseUriList() throws ServiceInvocationException {
        setMediaType(Media.TEXT_URI_LIST);// Set the mediatype to text/uri-list
        java.util.Set<URI> setOfUris = new java.util.HashSet<URI>();
        java.io.InputStreamReader isr = null;
        java.io.InputStream is = null;
        java.io.BufferedReader reader = null;
        try {
            if (con == null) {
                con = connect(uri);
            }
            is = getRemoteStream();
            isr = new java.io.InputStreamReader(is);
            reader = new java.io.BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    setOfUris.add(new URI(line));
                } catch (URISyntaxException ex) {
                    throw new ServiceInvocationException(
                            "The server returned an invalid URI : '" + line + "'", ex);
                }
            }
        } catch (IOException io) {
            ServiceInvocationException connectionExc =
                    new ServiceInvocationException("Input-Output error occured while connecting to "
                    + "the server", io);
            throw connectionExc;
        } finally {
            ServiceInvocationException serviceInvocationException = null;
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    serviceInvocationException = new ServiceInvocationException("The stream reader (SR) over the connection to the "
                            + "remote service at '" + getUri() + "' cannot close gracefully", ex);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ex) {
                    serviceInvocationException = new ServiceInvocationException("The input stream reader (ISR) over the connection to the "
                            + "remote service at '" + getUri() + "' cannot close gracefully", ex);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    serviceInvocationException = new ServiceInvocationException("The input stream (IS) over the connection to the "
                            + "remote service at '" + getUri() + "' cannot close gracefully", ex);
                }
            }
            if (serviceInvocationException != null) {
                throw serviceInvocationException;
            }
        }
        return setOfUris;
    }

    @Override
    public String getResponseContentType() throws ServiceInvocationException {
        String ct = getResponseHeader(RequestHeaders.CONTENT_TYPE);
        if (ct == null) {
            return null;
        }
        return ct.split(";")[0];

    }

    @Override
    public String getResponseHeader(String header) throws ServiceInvocationException {
        if (con == null) {
            initializeConnection(uri);
        }
        for (int i = 0;; i++) {
            String headerName = con.getHeaderFieldKey(i);
            String headerValue = con.getHeaderField(i);
            if (headerName == null && headerValue == null) {
                break;
            }
            if (headerName == null) {
                continue;
            } else {
                if (headerName.equalsIgnoreCase(header)) {
                    return headerValue;
                }

            }
        }
        return null;
    }
}
