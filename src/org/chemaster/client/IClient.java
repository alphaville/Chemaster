package org.chemaster.client;

import com.hp.hpl.jena.ontology.OntModel;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.chemaster.client.collection.Media;

/**
 * Generic interface for a client in ToxOtis. 
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public interface IClient extends Closeable {

    /** Standard UTF-8 Encoding */
    final String URL_ENCODING = "UTF-8";

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
     *      If any of the arguments is <code>null</code>.
     */
    IClient addHeaderParameter(String paramName, String paramValue) throws NullPointerException, IllegalArgumentException;


    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    void close() throws IOException;

    /**
     * Retrieve the specified media type which is the value for the <code>Accept</code>
     * HTTP Header.
     * @return
     * The accepted media type.
     */
    String getMediaType();

    /**
     * Get the body of the HTTP response as InputStream.
     * @return
     * InputStream for the remote HTTP response
     * @throws ToxOtisException
     * In case an error status code is received from the remote location.
     */
    InputStream getRemoteStream() throws ServiceInvocationException;

    /**
     * Get the HTTP status of the response
     * @return
     * Response status code.
     * @throws ToxOtisException
     * In case the connection cannot be established because a {@link ToxOtisException }
     * is thrown while a connection is attempted to the remote service.
     */
    int getResponseCode() throws ServiceInvocationException;


    /**
     * Get the response body as a String in the format specified in the Accept header
     * of the request.
     *
     * @return
     * String consisting of the response body (in a MediaType which results
     * from content negotiation, taking into account the Accept header of the
     * request)
     * @throws ToxOtisException
     * In case some communication, server or request error occurs.
     */
    String getResponseText() throws ServiceInvocationException;

    /**
     * Get the targetted URI
     * @return
     *      The target URI
     */
    java.net.URI getUri();

    /**
     * Specify the mediatype to be used in the <tt>Accept</tt> header.
     * @param mediaType
     * Accepted mediatype
     *
     * @see RequestHeaders#ACCEPT
     */
    IClient setMediaType(String mediaType);

    /**
     * Specify the mediatype to be used in the <tt>Accept</tt> header providing
     * an instance of {@link Media }.
     * @param mediaType
     * Accepted mediatype
     * @see RequestHeaders#ACCEPT
     */
    IClient setMediaType(Media mediaType);

    /**
     * Set the URI on which the GET method is applied.
     * @param vri
     * The URI that will be used by the client to perform the remote connection.
     */
    IClient setUri(java.net.URI uri);

    /**
     * Provide the target URI as a String
     * @param uri The target URI as a String.
     * @throws java.net.URISyntaxException In case the provided URI is syntactically
     * incorrect.
     */
    IClient setUri(String uri) throws URISyntaxException;

    /**
     * Get the response of the remote service as a Set of URIs. The media type of
     * the request, as specified by the <code>Accept</code> header is set to
     * <code>text/uri-list</code>.
     * @return
     * Set of URIs returned by the remote service.
     * @throws ToxOtisException
     * In case some I/O communication error inhibits the transimittance of
     * data between the client and the server or a some stream cannot close.
     */
    Set<java.net.URI> getResponseUriList() throws ServiceInvocationException;

    public WriteLock getConnectionLock();

    public ReadLock getReadLock();

    String getResponseHeader(String header) throws ServiceInvocationException;

    String getResponseContentType() throws ServiceInvocationException;
}
