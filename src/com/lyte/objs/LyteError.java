package com.lyte.objs;

import com.lyte.core.LyteContext;
import com.lyte.core.LyteScope;
import com.lyte.core.LyteStack;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by a0225785 on 6/23/2015.
 */
public class LyteError extends RuntimeException implements LyteValue<RuntimeException> {

  private ArrayList<String> mLineNumbers = new ArrayList<String>();

  public LyteError(String value) {
    super(value);
  }

  public LyteError(LyteValue value) {
    super(value.is("error") ? ((LyteError) value).getMessage() : value.toString());
  }

  @Override
  public RuntimeException get() {
    return this;
  }

  @Override
  public void set(RuntimeException newValue) {
    throw new LyteError("Cannot set the value of an Error Object!");
  }

  @Override
  public LyteValue getProperty(String property) {
    // TODO Implement Logic
    return null;
  }

  @Override
  public void setProperty(String property, LyteValue newValue) {
    throw new LyteError("Cannot set the property " + property + " of an Error Object!");
  }

  @Override
  public boolean hasProperty(String property) {
    // TODO Implement Logic
    return false;
  }

  @Override
  public boolean toBoolean() {
    return true;
  }

  @Override
  public double toNumber() {
    return this.hashCode();
  }

  @Override
  public String typeOf() {
    return "error";
  }

  @Override
  public LyteValue<RuntimeException> clone(LyteContext context) {
    return this;
  }

  @Override
  public LyteValue apply(LyteContext context) {
    return this;
  }

  @Override
  public String toString() {
    return (!mLineNumbers.isEmpty() ? "Line " + mLineNumbers.get(0) + ": " : "") + getMessage();
  }

  public void addLineNumber(String lineNumber) {
    mLineNumbers.add(lineNumber);
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
    return this == other;
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
