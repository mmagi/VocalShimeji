package com.group_finity.mascot.util;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Magi
 * Date: 13-1-12
 * Time: 上午10:54
 * To change this template use File | Settings | File Templates.
 */
public class QueList<T> implements Iterable<T> {

    final ListHeaderNode<T> head = new ListHeaderNode<T>();
    @Override
    public Iterator<T> iterator() {
        return new ListIterator<T>();
    }

    public Iterator<T> circleIterator(){
        return new ListCircleIterator<T>();
    }
    public void offer(T e){
        ListNode<T> node = new ListNode<T>(e);
        synchronized (head){
            node.next = head.next;
            head.next = node;
            if (node.next == head){
                head.notifyAll();
            }
        }
    }
    class ListCircleIterator<T> implements Iterator<T>{
        @SuppressWarnings("unchecked")
        protected ListNode<T> cur = (ListNode<T>) head;
        @SuppressWarnings("unchecked")
        protected ListNode<T> last = (ListNode<T>) null;
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public T next() {
            last = cur;
            cur = last.getNext();
            if(cur.deleted){
                do{
                    cur = cur.getNext();
                }while (cur.deleted);
                last.setNext(cur);
            }
            T next = cur.getObject();
            if (null == next){//是head节点
                return next();
            }
            return cur.getObject();

        }

        @Override
        public void remove() {
            cur.delete();
        }
    }
    class ListIterator<T> extends ListCircleIterator<T>{
        @Override
        public boolean hasNext(){
            return (cur.next != head) || (last==null);
        }
    }
    /**
     * Created with IntelliJ IDEA.
     * User: Magi
     * Date: 13-1-12
     * Time: 上午10:55
     *
     */

    private class ListNode<T> {
        volatile protected ListNode<T> next;
        ListNode<T> getNext(){
            return next;
        }
        void setNext(ListNode<T> next){
            this.next = next;
        }
        private final T object;
        protected ListNode(T o){
            if (null == o) throw new NullPointerException();
            object=o;
        }
        private ListNode(){
            object=null;
        }
        T getObject(){
            return object;
        }
        volatile private boolean deleted = false;
        void delete(){
            deleted = true;
        }
    }

    private class ListHeaderNode<T> extends ListNode<T> {
        ListHeaderNode() {
            next = this;
        }
        @Override
        ListNode<T> getNext(){
            while (this.next == null){
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
            return this.next;
        }
        @Override
        void delete(){
            throw new UnsupportedOperationException();
        }
    }
}
