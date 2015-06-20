package com.lyte.core;

import com.lyte.objs.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by a0225785 on 6/17/2015.
 */
public class LyteInvokeStatement implements LyteStatement {

  private String mPrimaryIdentifier;
  private List<LyteSpecifier> mSpecifiers;

  public LyteInvokeStatement(String primaryIdentifier) {
    this(primaryIdentifier, new ArrayList<LyteSpecifier>());
  }

  public LyteInvokeStatement(String primaryIdentifier, List<LyteSpecifier> specifiers) {
    mPrimaryIdentifier = primaryIdentifier;
    mSpecifiers = specifiers;
  }

  @Override
  public void applyTo(LyteScope scope, LyteStack stack) {
    if (isSimpleInvokation()) {
      LyteValue value = scope.getVariable(mPrimaryIdentifier);
      if (value.typeOf().equals("block")) {
        ((LyteBlock) value).invoke(stack);
      } else {
        stack.push(value);
      }
    } else {
      stack.push(resolveToValue(scope, stack));
    }
  }

  public boolean isSimpleInvokation() {
    return mSpecifiers.isEmpty();
  }

  public String getPrimaryIdentifier() {
    return mPrimaryIdentifier;
  }

  public LyteSpecifier getLastSpecifier() {
    return mSpecifiers.get(mSpecifiers.size() - 1);
  }


  public LyteValue resolveToValue(LyteScope scope, LyteStack stack) {
    LyteValue obj;
    try {
      obj = scope.getVariable(mPrimaryIdentifier);
      // Process all specifiers including the last one
      for (LyteSpecifier specifier : mSpecifiers) {
        if (specifier.identifier != null) {
          obj = ((LyteObject) obj).get(specifier.identifier);
        } else if (specifier.invokable != null) {
          // TODO Ensure only one result is pushed onto the stack
          specifier.invokable.applyTo(scope, stack);
          obj = ((LyteObject) obj).get(stack.pop().toString());
        } else {
          List<LyteValue> arguments = new ArrayList<LyteValue>();
          // TODO Check if the ordering is correct & ensure that the block only has one result
          // Add a clone of each of the arguments to the function
          for (int i = 0; i < specifier.arguments.size(); i++) {
            arguments.add(specifier.arguments.get(i).clone(scope));
          }
          // Then invoke the block itself w/ those arguments
          ((LyteBlock) obj).invoke(stack, arguments);
          // And pop the result into the object
          obj = stack.pop();
        }
      }
    } catch (Exception e) {
      System.err.println("Cannot resolve " + toString(false));
      return LyteUndefined.UNDEFINED;
    }
    return obj;
  }

  public LyteValue resolveToObject(LyteScope scope, LyteStack stack) {
    LyteValue obj;
    try {
      obj = scope.getVariable(mPrimaryIdentifier);
      // Process all specifiers up to the last one
      for (int i = 0; i < (mSpecifiers.size() - 1); i++) {
        LyteSpecifier specifier = mSpecifiers.get(i);

        if (specifier.identifier != null) {
          obj = ((LyteObject) obj).get(specifier.identifier);
        } else if (specifier.invokable != null) {
          // TODO Ensure only one result is pushed onto the stack
          specifier.invokable.applyTo(scope, stack);
          obj = ((LyteObject) obj).get(stack.pop().toString());
        } else {
          List<LyteValue> arguments = new ArrayList<LyteValue>();
          // TODO Check if the ordering is correct & ensure that the block only has one result
          // Add a clone of each of the arguments to the function
          for (LyteRawBlock argument : specifier.arguments) {
            arguments.add(argument.clone(scope));
          }
          // Then invoke the block itself w/ those arguments
          ((LyteBlock) obj).invoke(stack, arguments);
          // And pop the result into the object
          obj = stack.pop();
        }
      }
    } catch (Exception e) {
      System.err.println("Cannot resolve " + toString(false));
      return LyteUndefined.UNDEFINED;
    }
    return obj;
  }

  public boolean isFunctionInvokation() {
    return !isSimpleInvokation() && (getLastSpecifier().arguments != null);
  }

  public String toString(boolean withDescription) {
    if (withDescription) {
      return "Invoke: " + mPrimaryIdentifier + mSpecifiers;
    } else {
      return mPrimaryIdentifier + mSpecifiers;
    }
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public static class LyteSpecifier {
    public final String identifier;
    public final LyteStatement invokable;
    public final List<LyteRawBlock> arguments;

    private LyteSpecifier(String identifier, LyteStatement invokable, List<LyteRawBlock> arguments) {
      this.identifier = identifier;
      this.invokable = invokable;
      this.arguments = arguments;
    }

    public LyteSpecifier(String identifier) {
      this(identifier, null, null);
    }

    public LyteSpecifier(LyteStatement invokable) {
      this(null, invokable, null);
    }

    public LyteSpecifier(List<LyteRawBlock> arguments) {
      this(null, null, arguments);
    }

    @Override
    public String toString() {
      if (identifier != null) {
        return "." + identifier;
      } else if (invokable != null) {
        return "[" + invokable + "]";
      } else {
        return "(" + arguments + ")";
      }
    }
  }
}
