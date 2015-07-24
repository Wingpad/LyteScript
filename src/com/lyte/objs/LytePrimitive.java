package com.lyte.objs;

import com.lyte.core.LyteContext;
import com.lyte.core.LyteScope;
import com.lyte.core.LyteStack;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by jszaday on 6/18/15.
 */
public abstract class LytePrimitive<T> implements LyteValue<T> {

  private T mValue;

  public LytePrimitive(T value) {
    mValue = value;
  }

  @Override
  public T get() {
    return mValue;
  }

  @Override
  public LyteValue getProperty(String property) {
    throw new LyteError("Cannot get property " + property + " from a(n) " + typeOf() + "!");
  }

  @Override
  public void setProperty(String property, LyteValue newValue) {
    throw new LyteError("Cannot set property " + property + " of a(n) " + typeOf() + "!");
  }

  @Override
  public boolean hasProperty(String property) {
    return false;
  }

  @Override
  public LyteValue apply(LyteContext context) {
    return this;
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
  public LyteValue<T> clone(LyteContext context) {
    return this;
  }

  @Override
  public boolean equals(LyteValue other) {
    if (other.is(typeOf())) {
      return equalsStrict(other);
    } else if (other.isSimpleComparison()) {
      return other.equals(this);
    } else {
      return false;
    }
  }

  @Override
  public boolean equalsStrict(LyteValue other) {
    return other.is(typeOf()) && other.get().equals(get());
  }

  @Override
  public boolean isSimpleComparison() {
    return false;
  }

  @Override
  public boolean is(String type) {
    return type.equals(typeOf());
  }

  @Override
  public Set<String> getProperties() {
    throw new LyteError("Cannot get the properties of " + typeOf() + "!");
  }
}