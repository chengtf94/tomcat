package org.apache.catalina;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * 处理点：责任链模式
 *
 * @author Craig R. McClanahan
 * @author Gunnar Rjnning
 * @author Peter Donald
 */
public interface Valve {

    void setNext(Valve valve);
    Valve getNext();

    void invoke(Request request, Response response) throws IOException, ServletException;

    void backgroundProcess();

    boolean isAsyncSupported();

}
