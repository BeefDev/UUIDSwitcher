package io.github.beefdev.uuidswitcher.core.internal.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public class SynchronizedListWrapper<E> implements List<E> {

    private final List<E> originalList;
    private final Consumer<E> elementAdditionHandler;

    public SynchronizedListWrapper(List<E> originalList, Consumer<E> elementAdditionHandler) {
        this.originalList = originalList;
        this.elementAdditionHandler = elementAdditionHandler;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().contains(o);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return this.getOriginalList().iterator();
    }

    @Override
    public Object[] toArray() {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().toArray();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().toArray(a);
        }
    }

    @Override
    public boolean add(E e) {
        synchronized (this.getOriginalList()) {
            this.handleElementAddition(e);
            return this.getOriginalList().add(e);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().remove(o);
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().containsAll(c);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for(E element : c) {
            this.handleElementAddition(element);
        }

        return this.getOriginalList().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        synchronized (this.getOriginalList()) {
            for(E element : c) {
                this.handleElementAddition(element);
            }

            return this.getOriginalList().addAll(index, c);
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().removeAll(c);
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().retainAll(c);
        }
    }

    @Override
    public void clear() {
        synchronized (this.getOriginalList()) {
            this.getOriginalList().clear();
        }
    }

    @Override
    public E get(int index) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().get(index);
        }
    }

    @Override
    public E set(int index, E element) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().set(index, element);
        }
    }

    @Override
    public void add(int index, E element) {
        synchronized (this.getOriginalList()) {
            this.handleElementAddition(element);
            this.getOriginalList().add(index, element);
        }
    }

    @Override
    public E remove(int index) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().remove(index);
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().lastIndexOf(o);
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        return this.getOriginalList().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return this.getOriginalList().listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        synchronized (this.getOriginalList()) {
            return this.getOriginalList().subList(fromIndex, toIndex);
        }
    }

    private List<E> getOriginalList() {
        return this.originalList;
    }

    private void handleElementAddition(E element) {
        this.elementAdditionHandler.accept(element);
    }
}
