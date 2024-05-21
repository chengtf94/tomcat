package org.apache.tomcat.util.collections;

/**
 * SynchronizedStack对象池
 */
public class SynchronizedStack<T> {
    public static final int DEFAULT_SIZE = 128;
    private static final int DEFAULT_LIMIT = -1;

    /** 内部维护⼀个对象数组，⽤数组实现栈的功能 */
    private int size;
    private final int limit;
    private int index = -1;
    private Object[] stack;

    /**
     * 构造方法
     */
    public SynchronizedStack() {
        this(DEFAULT_SIZE, DEFAULT_LIMIT);
    }
    public SynchronizedStack(int size, int limit) {
        if (limit > -1 && size > limit) {
            this.size = limit;
        } else {
            this.size = size;
        }
        this.limit = limit;
        stack = new Object[this.size];
    }

    /** 归还对象 */
    public synchronized boolean push(T obj) {
        index++;
        if (index == size) {
            if (limit == -1 || size < limit) {
                expand();
            } else {
                index--;
                return false;
            }
        }
        stack[index] = obj;
        return true;
    }

    /** 获取对象 */
    @SuppressWarnings("unchecked")
    public synchronized T pop() {
        if (index == -1) {
            return null;
        }
        T result = (T) stack[index];
        stack[index--] = null;
        return result;
    }

    /** 扩容：以以2倍⼤⼩扩展  */
    private void expand() {
        int newSize = size * 2;
        if (limit != -1 && newSize > limit) {
            newSize = limit;
        }
        Object[] newStack = new Object[newSize];
        System.arraycopy(stack, 0, newStack, 0, size);
        // garbage.
        stack = newStack;
        size = newSize;
    }

    public synchronized void clear() {
        if (index > -1) {
            for (int i = 0; i < index + 1; i++) {
                stack[i] = null;
            }
        }
        index = -1;
    }

}
