package com.lyte.core;

import java.util.HashMap;
import java.util.HashSet;

import com.lyte.objs.LyteError;
import com.lyte.objs.LyteObject;
import com.lyte.objs.LyteValue;
import com.lyte.utils.LyteInjectable;

public class LyteScope implements LyteInjectable {
  private HashMap<String, LyteValue> mVariables;
  private HashSet<String> mFinalVariables;
  private LyteScope mParent;

  private LyteScope(LyteScope parent, boolean useParentStack) {
    mParent = parent;
    mVariables = new HashMap<String, LyteValue>();
    mFinalVariables = new HashSet<String>();
  }

  private LyteScope(LyteScope parent) {
    this(parent, true);
  }

  public LyteValue getVariable(String name) {
    if (mVariables.containsKey(name)) {
      return mVariables.get(name);
    } else if (mParent != null) {
      return mParent.getVariable(name);
    } else {
      throw new LyteError("Undefined variable, " + name);
    }
  }

  public boolean hasVariable(String name) {
    if (mVariables.containsKey(name)) {
      return true;
    } else if (mParent != null) {
      return mParent.hasVariable(name);
    } else {
      return false;
    }
  }

  public void putVariable(String name, LyteValue value, boolean finalVariable) {
    if (mParent != null && mParent.hasVariable(name)) {
      mParent.putVariable(name, value, finalVariable);
    } else {
      if (mFinalVariables.contains(name)) {
        throw new LyteError("Cannot override the value of " + name);
      } else {
        mVariables.put(name, value);

        if (finalVariable) {
          mFinalVariables.add(name);
        }
      }
    }
  }

  public void finalizeVariable(String name) {
    mFinalVariables.add(name);
  }

  public LyteScope enter() {
    return new LyteScope(this);
  }

  public LyteScope leave() {
    return mParent;
  }

  public LyteScope clone() {
    LyteScope newSibling = mParent.enter();
    // TODO Determine Implementation
    return newSibling;
  }

  public static LyteScope newGlobal() {
    return new LyteScope(null, false);
  }

  public String getStackTrace() {
    return toString() + " called by:\n" + getStackTrace(mParent);
  }

  private static String getStackTrace(LyteScope scope) {
    return scope.toString() + "\n" + (scope.mParent != null ? getStackTrace(scope.mParent) : "");
  }

  public void printStackTrace() {
    System.out.print(getStackTrace());
  }

  public void dump() {
    if (mParent != null) {
      mParent.dump();
      System.out.println(toString() + " : " + mVariables);
    }
  }

  @Override
  public void inject(String name, LyteValue value) {
    putVariable(name, value, true);
  }
}
