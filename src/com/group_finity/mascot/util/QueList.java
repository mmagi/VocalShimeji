package com.group_finity.mascot.util;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Magi
 * Date: 13-1-12
 * Time: 上午10:54
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

        private boolean preFetched = false;

        private void preFetch() {
            if (!preFetched) {
                last = cur;
                cur = last.fetchNext();
                preFetched = true;
            }
        }

        @Override
        public boolean hasNext() {
            preFetch();
            return cur != head;
        }

        @Override
        public T next() {
            preFetch();
            preFetched = false;
            return cur.object;
        }

        @Override
        public void remove() {
            if (preFetched) {
                last.markDeleted();
            } else {
                cur.markDeleted();
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
            cur = last.fetchNextB();
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

        ListNode<T> fetchNext() {
            ListNode<T> next = this.next;
            if (next.deleted) {
                do {
                    next = next.next;
                } while (next.deleted);
                this.next = next;
            }
            return next;
        }

        private final T object;

        protected ListNode(T o) {
            if (null == o)
                throw new NullPointerException();
            object = o;
        }

        private ListNode() {
            object = null;
        }

        volatile private boolean deleted = false;

        void markDeleted() {
            deleted = true;
        }

        ListNode<T> fetchNextB() {
            return fetchNext();
        }
    }

    private class ListHeaderNode<T> extends ListNode<T> {
        ListHeaderNode() {
            next = this;
        }

        @Override
        synchronized ListNode<T> fetchNext() {
            return super.fetchNext();
        }

        @Override
        void markDeleted() {
            throw new UnsupportedOperationException();
        }

        /**
         * fetchNext的阻塞版本，如果队列空则阻塞，直到至少有一个元素
         *
         * @return 下一个不是head的节点
         */
        synchronized ListNode<T> fetchNextB() {
            ListNode<T> next;
            while (this == (next = super.fetchNext())) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
            return next;
        }
    }
}
