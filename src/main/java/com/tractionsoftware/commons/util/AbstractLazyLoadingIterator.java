/*
 *
 *    Copyright 1996-2026 Traction Software, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.

package com.tractionsoftware.commons.util;

import com.google.common.annotations.Beta;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A skeleton implementation of {@link Iterator} that loads results in a lazy manner.
 *
 * @param <T>
 *     the type of elements returned by this iterator
 * @author Dave Shepperton
 */
@Beta
public abstract class AbstractLazyLoadingIterator<T> implements Iterator<T> {

    private static enum State {
        WAITING,
        READY,
        EXHAUSTED
    }

    private State state;

    private T nextValue;

    public AbstractLazyLoadingIterator() {
        super();
        this.state = State.WAITING;
    }

    @Override
    public final boolean hasNext() {
        ensureReady();
        if (state == State.READY) {
            return true;
        }
        return false;
    }

    @Override
    public final T next() {
        if (hasNext()) {
            return consumeNextValue();
        }
        throw new NoSuchElementException();
    }

    private final T consumeNextValue() {
        T ret = nextValue;
        state = State.WAITING;
        nextValue = null;
        return ret;
    }

    private final void ensureReady() {
        if (state == State.WAITING) {
            nextValue = loadNextValue();
            if (nextValue == null) {
                state = State.EXHAUSTED;
            }
            else {
                state = State.READY;
            }
        }
    }

    protected abstract T loadNextValue();

}
