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
    final Iterable<T> circle = new Iterable<T>() {
        @Override
        public Iterator<T> iterator() {
            return new ListCircleIterator<T>();
        }
    };

    @Override
    public Iterator<T> iterator() {
        return new ListIterator<T>();
    }

    public Iterable<T> asCircle() {
        return circle;
    }

    public void offer(T e) {
        ListNode<T> node = new ListNode<T>(e);
        synchronized (head) {
            node.next = head.next;
            head.next = node;
            if (node.next == head) {
                head.notifyAll();
            }
        }
    }

    class ListIterator<T> implements Iterator<T> {
        @SuppressWarnings("unchecked")
        protected ListNode<T> cur = (ListNode<T>) head;
        @SuppressWarnings("unchecked")
        protected ListNode<T> last = (ListNode<T>) null;

        private boolean notReady = true;
        private T nextObj = null;

        private void prepareNext() {
            last = cur;
            cur = last.next;
            if (cur.deleted) {
                do {
                    cur = cur.next;
                } while (cur.deleted);
                last.setNext(cur);
            }
            notReady = false;
            nextObj = cur.object;
        }

        @Override
        public boolean hasNext() {
            if (notReady) prepareNext();
            return nextObj != null;
        }

        @Override
        public T next() {
            if (notReady) prepareNext();
            notReady = true;
            return nextObj;
        }

        @Override
        public void remove() {
            if (notReady) {
                cur.markDeleted();
            } else {
                last.markDeleted();
            }
        }
    }

    class ListCircleIterator<T> implements Iterator<T> {
        @SuppressWarnings("unchecked")
        protected ListNode<T> cur = (ListNode<T>) head;
        @SuppressWarnings("unchecked")
        protected ListNode<T> last = (ListNode<T>) null;

        /**
         * 无限循环
         *
         * @return true
         */
        @Override
        public final boolean hasNext() {
            return true;
        }

        /**
         * 返回队里中下一个对象，或在一圈循环之后返回一次null。
         * 若某次循环开始时队列已空，这个方法将一直阻塞直到队里中至少有一个元素后继续循环。
         *
         * @return 队列中的下一个对象，或者当一圈循环完之后返回一次null
         */
        @Override
        public final T next() {
            last = cur;
            cur = last.getNext();
            if (cur.deleted) {
                do {
                    cur = cur.getNext();
                } while (cur.deleted);
                last.setNext(cur);
            }
            return cur.object;
        }

        /**
         * 标记当前元素为待删除，下一次循环时不再会返回此元素。
         */
        @Override
        public void remove() {
            cur.markDeleted();
        }
    }

    /**
     *
     *
     */

    private class ListNode<T> {
        volatile protected ListNode<T> next;

        ListNode<T> getNext() {
            return next;
        }

        void setNext(ListNode<T> next) {
            this.next = next;
        }

        private final T object;

        protected ListNode(T o) {
            if (null == o) throw new NullPointerException();
            object = o;
        }

        private ListNode() {
            object = null;
        }

        volatile private boolean deleted = false;

        void markDeleted() {
            deleted = true;
        }
    }

    private class ListHeaderNode<T> extends ListNode<T> {
        ListHeaderNode() {
            next = this;
        }

        @Override
        ListNode<T> getNext() {
            while (this.next == this) {
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException ignored) {
                }
            }
            return this.next;
        }

        @Override
        void markDeleted() {
            throw new UnsupportedOperationException();
        }
    }
}
