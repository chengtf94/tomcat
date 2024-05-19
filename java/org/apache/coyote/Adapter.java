package org.apache.coyote;

import org.apache.tomcat.util.net.SocketEvent;

/**
 * Adapter：负责Tomcat Request/Response与ServletRequest/ServletResponse的转化，提供ServletRequest对象给容器。
 *
 * @author Remy Maucherat
 */
public interface Adapter {

    /**
     * Call the service method, and notify all listeners
     */
    void service(Request req, Response res) throws Exception;

    /**
     * Prepare the given request/response for processing. This method requires that the request object has been
     * populated with the information available from the HTTP headers.
     */
    boolean prepare(Request req, Response res) throws Exception;

    boolean asyncDispatch(Request req, Response res, SocketEvent status) throws Exception;

    void log(Request req, Response res, long time);

    void checkRecycled(Request req, Response res);

    String getDomain();

}
