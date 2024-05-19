package org.apache.coyote;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;

/**
 * Processor：负责应⽤层协议解析，提供Tomcat Request对象给Adapter。
 */
public interface Processor {

    /**
     * Process a connection. This is called whenever an event occurs (e.g. more data arrives) that allows processing to
     * continue for a connection that is not currently being processed.
     */
    SocketState process(SocketWrapperBase<?> socketWrapper, SocketEvent status) throws IOException;

    /**
     * Generate an upgrade token.
     */
    UpgradeToken getUpgradeToken();

    boolean isUpgrade();

    boolean isAsync();

    /**
     * Check this processor to see if the timeout has expired and process a timeout if that is that case.
     */
    void timeoutAsync(long now);

    /**
     * @return The request associated with this processor.
     */
    Request getRequest();

    /**
     * Recycle the processor, ready for the next request which may be on the same connection or a different connection.
     */
    void recycle();

    /**
     * Set the SSL information for this HTTP connection.
     */
    void setSslSupport(SSLSupport sslSupport);

    /**
     * Allows retrieving additional input during the upgrade process.
     */
    ByteBuffer getLeftoverInput();

    /**
     * Informs the processor that the underlying I/O layer has stopped accepting new connections. This is primarily
     * intended to enable processors that use multiplexed connections to prevent further 'streams' being added to an
     * existing multiplexed connection.
     */
    void pause();

    /**
     * Check to see if the async generation (each cycle of async increments the generation of the AsyncStateMachine) is
     * the same as the generation when the most recent async timeout was triggered. This is intended to be used to avoid
     * unnecessary processing.
     */
    boolean checkAsyncTimeoutGeneration();

}
