package com.tractionsoftware.commons.util;

import com.google.common.annotations.Beta;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@link Supplier} that caches the instance until the backing resource has changed.
 *
 * @param <T>
 *     the type of object.
 */
@Beta
public abstract class ReloadingSupplier<T> implements Supplier<T> {

    private T obj;

    private Date lastLoadTime;

    protected ReloadingSupplier(@Nullable T initialObj, @Nullable Date loadTime) {
        this.obj = initialObj;
        this.lastLoadTime = Objects.requireNonNullElseGet(loadTime, () -> new Date(0));
    }

    @Nonnull
    @Override
    public final T get() {
        reloadIfModified();
        return getCurrent();
    }

    public abstract String toDebugString();

    protected abstract Date getResourceLastModifiedDate();

    protected abstract T reload();

    protected synchronized final T getCurrent() {
        return obj;
    }

    private final void reloadIfModified() {
        if (hasChanged()) {
            Date loadTime = new Date();
            T newObj = reload();
            update(newObj, loadTime);
        }
    }

    private final boolean hasChanged() {
        if (getResourceLastModifiedDate().equals(lastLoadTime)) {
            return false;
        }
        return true;
    }

    private synchronized final void update(T updated, Date newLoadTime) {
        obj = updated;
        lastLoadTime = newLoadTime;
    }

}
