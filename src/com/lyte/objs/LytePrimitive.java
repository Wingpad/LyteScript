package com.lyte.objs;

import com.lyte.core.LyteScope;

/**
 * Created by jszaday on 6/18/15.
 */
public abstract class LytePrimitive<T> implements LyteValue {

    private T mValue;

    public LytePrimitive(T value) {
        mValue = value;
    }

    public void set(T value) {
        mValue = value;
    }

    public T get() {
        return mValue;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }

    @Override
    public String typeOf() {
        return mValue.getClass().getSimpleName().toLowerCase();
    }

    @Override
    public LyteValue clone(LyteScope scope) {
        return this;
    }
}