package ait.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    LinkedList<T> queue;
    Lock mutex = new ReentrantLock();
    Condition senderWaitingCondition = mutex.newCondition();
    Condition receiverWaitingCondition = mutex.newCondition();
    int size;

    public BlkQueueImpl(int size) {
        queue = new LinkedList<>();
        this.size = size;
    }

    @Override
    public void push(T message) {
        mutex.lock();
        try {
            while (queue.size() >= size) {
                try {
                    senderWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.push(message);
            receiverWaitingCondition.signal();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public T pop() {
        mutex.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    receiverWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T message = queue.pop();
            senderWaitingCondition.signal();
            return message;
        } finally {
            mutex.unlock();
        }
    }
}
