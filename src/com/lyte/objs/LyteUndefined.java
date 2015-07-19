package com.lyte.objs;

import com.lyte.core.LyteContext;
import com.lyte.core.LyteScope;
import com.lyte.core.LyteStack;

import java.util.Set;

/**
 * Created by jszaday on 6/19/15.
 */
public enum LyteUndefined implements LyteValue {

  NULL("null"), UNDEFINED("undefined");

  private String mType;

  LyteUndefined(String value) {
    mType = value;
  }

  @Override
  public String typeOf() {
    return mType;
  }

  @Override
  public String toString() {
    return mType;
  }

  @Override
  public boolean toBoolean() {
    return false;
  }

  @Override
  public double toNumber() {
    return 0;
  }


  @Override
  public Object get() {
    return this;
  }

  @Override
  public LyteValue getProperty(String property) {
    throw new LyteError("Cannot get property " + property + " from a(n) " + typeOf() + "!");
  }

  @Override
  public void setProperty(String property, LyteValue newValue) {
    throw new LyteError("Cannot set property " + property + " from a(n) " + typeOf() + "!");
  }

  @Override
  public boolean hasProperty(String property) {
    return false;
  }

  @Override
  public LyteValue clone(LyteContext scope) {
    return this;
  }

  @Override
  public LyteValue apply(LyteContext stack) {
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
    return other == this;
  }

  @Override
  public boolean isSimpleComparison() {
    return true;
  }

  @Override
  public boolean is(String type) {
    return type.equals(typeOf());
  }

  @Override
  public Set<String> getProperties() {
    throw new LyteError("Cannot get the properties of " + typeOf() + "!");
  }

  @Override
  public String toJSONString() {
    if (this == NULL) {
      return toString();
    } else {
      throw new LyteError("Can't encode the value " + toString() + " as JSON!");
    }
  }

  @Override
  public LyteBlock generator() {
    throw new LyteError("Cannot iterate over a(n) " + typeOf());
  }
}
