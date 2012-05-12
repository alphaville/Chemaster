package org.chemaster.client;

import java.net.URI;
import org.chemaster.client.http.GetHttpClient;
import org.chemaster.client.http.PostHttpClient;

/**
 * Factory for creating clients. 
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class ClientFactory {

    /**
     * Create a Get-client as an instance of {@link IGetClient } providing it's
     * URI. Either an HTTP or an HTTPS client is created according to the protocol
     * of the provided URI.
     * @param actionUri
     *      The URI on which the client addresses the request.
     * @return
     *      Instance of a GET-client.
     */
    public static IGetClient createGetClient(URI actionUri) {
        return new GetHttpClient(actionUri);
    }

    /**
     * Create a POST-client as an instance of {@link IPostClient } providing it's
     * URI. Either an HTTP or an HTTPS client is created according to the protocol
     * of the provided URI.
     * @param actionUri
     *      The URI on which the client addresses the request.
     * @return
     *      Instance of a POST-client.
     */
    public static IPostClient createPostClient(URI actionUri) {
        return new PostHttpClient(actionUri);
    }

    
}
