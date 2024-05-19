package org.apache.coyote.http11;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;

/**
 * HTTP/1.1 protocol implementation using NIO.
 */
public class Http11NioProtocol extends AbstractHttp11JsseProtocol<NioChannel> {

    private static final Log log = LogFactory.getLog(Http11NioProtocol.class);


    public Http11NioProtocol() {
        this(new NioEndpoint());
    }


    public Http11NioProtocol(NioEndpoint endpoint) {
        super(endpoint);
    }


    @Override
    protected Log getLog() {
        return log;
    }


    // -------------------- Pool setup --------------------

    @Deprecated
    public void setPollerThreadCount(int count) {
    }

    @Deprecated
    public int getPollerThreadCount() {
        return 1;
    }

    public void setSelectorTimeout(long timeout) {
        ((NioEndpoint) getEndpoint()).setSelectorTimeout(timeout);
    }

    public long getSelectorTimeout() {
        return ((NioEndpoint) getEndpoint()).getSelectorTimeout();
    }

    public void setPollerThreadPriority(int threadPriority) {
        ((NioEndpoint) getEndpoint()).setPollerThreadPriority(threadPriority);
    }

    public int getPollerThreadPriority() {
        return ((NioEndpoint) getEndpoint()).getPollerThreadPriority();
    }


    // ----------------------------------------------------- JMX related methods

    @Override
    protected String getNamePrefix() {
        if (isSSLEnabled()) {
            return "https-" + getSslImplementationShortName() + "-nio";
        } else {
            return "http-nio";
        }
    }
}
