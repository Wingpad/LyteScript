package com.lyte.objs;

import com.lyte.core.LyteScope;

/**
 * Created by a0225785 on 6/23/2015.
 */
public class LyteError extends RuntimeException implements LyteValue<RuntimeException> {

  public LyteError(String value) {
    super(value);
  }

  public LyteError(LyteValue value) {
    super(value.toString());
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
  public LyteValue<RuntimeException> clone(LyteScope scope) {
    return this;
  }

  @Override
  public LyteValue apply(LyteValue self) {
    return this;
  }

  @Override
  public String toString() {
    return getMessage();
  }
}