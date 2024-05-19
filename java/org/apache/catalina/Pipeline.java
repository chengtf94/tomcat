package org.apache.catalina;

import java.util.Set;

/**
 * Pipeline管道：维护Valve链表
 *
 * @author Craig R. McClanahan
 * @author Peter Donald
 */
public interface Pipeline extends Contained {

    /**
     * BasicValve：处于Valve链表的末端，它是Pipeline中必不可少的⼀个Valve，负责调⽤下层容器的Pipeline⾥的第⼀个Valve
     */
    void setBasic(Valve valve);
    Valve getBasic();

    void addValve(Valve valve);

    Valve getFirst();

    Valve[] getValves();

    void removeValve(Valve valve);

    boolean isAsyncSupported();

    void findNonAsyncValves(Set<String> result);

}
