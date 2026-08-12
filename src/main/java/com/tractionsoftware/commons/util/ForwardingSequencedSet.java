package com.tractionsoftware.commons.util;

import com.google.common.collect.ForwardingSet;
import jakarta.annotation.Nonnull;

import java.util.SequencedSet;
import java.util.Spliterator;

public abstract class ForwardingSequencedSet<E> extends ForwardingSet<E> implements SequencedSet<E> {

    /**
     * Constructor for use by subclasses.
     */
    protected ForwardingSequencedSet() {
    }

    @Nonnull
    @Override
    protected abstract SequencedSet<E> delegate();

    @Nonnull
    @Override
    public SequencedSet<E> reversed() {
        return delegate().reversed();
    }

    @Override
    public void addFirst(E e) {
        delegate().addFirst(e);
    }

    @Override
    public void addLast(E e) {
        delegate().addLast(e);
    }

    @Override
    public E getFirst() {
        return delegate().getFirst();
    }

    @Override
    public E getLast() {
        return delegate().getLast();
    }

    @Override
    public E removeFirst() {
        return delegate().removeFirst();
    }

    @Override
    public E removeLast() {
        return delegate().removeLast();
    }

    @Nonnull
    @Override
    public Spliterator<E> spliterator() {
        return delegate().spliterator();
    }

}
