package com.tractionsoftware.commons.util;

import com.google.common.collect.ForwardingCollection;
import jakarta.annotation.Nonnull;

import java.util.SequencedCollection;

public abstract class ForwardingSequencedCollection<E> extends ForwardingCollection<E> implements SequencedCollection<E> {

    @Nonnull
    @Override
    protected abstract SequencedCollection<E> delegate();

    @Nonnull
    @Override
    public SequencedCollection<E> reversed() {
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

}
