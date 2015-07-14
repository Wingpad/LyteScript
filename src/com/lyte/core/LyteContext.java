package com.lyte.core;

import com.lyte.objs.*;
import com.lyte.stdlib.LyteReflectionFunctions;
import com.lyte.utils.LyteInjectable;
import org.apache.commons.collections4.iterators.PeekingIterator;

import java.util.ArrayList;
import java.util.List;

import static com.lyte.core.LyteInvokeStatement.LyteSpecifier;

/**
 * Created by a0225785 on 6/30/2015.
 */
public class LyteContext implements LyteInjectable {
  public LyteValue self;
  public LyteScope scope;
  public LyteStack stack;

  public LyteContext(LyteValue self, LyteScope scope, LyteStack stack) {
    this.self = self;
    this.scope = scope;
    this.stack = stack;
  }

  public LyteContext(LyteValue self, LyteContext context) {
    this(self, context.scope, context.stack);
  }

  public LyteContext(LyteValue self, LyteValue... args) {
    this(self, null, new LyteStack(args));
  }

  public void push(String string) {
    push(new LyteString(string));
  }

  public void push(Double number) {
    push(LyteNumber.valueOf(number));
  }

  public void push(Boolean bool) {
    push(LyteBoolean.valueOf(bool));
  }

  public void push(Integer integer) {
    push(LyteNumber.valueOf(integer));
  }

  public void push(Long number) {
    push(LyteNumber.valueOf(number.doubleValue()));
  }

  public void push(LyteValue value) {
    stack.push(value);
  }

  public LyteValue pop() {
    return stack.pop();
  }

  public LyteValue peek() {
    return stack.peek();
  }

  public LyteValue apply() {
    return stack.pop().apply(this);
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }

  public LyteValue get(String name) {
    if (name.startsWith("@")) {
      if (self == null) {
        throw new LyteError("Cannot resolve property " + name);
      }
      return self.getProperty(name.substring(1, name.length()));
    } else if (name.startsWith("#")) {
      if (stack == null || stack.isEmpty()) {
        throw new LyteError("Cannot resolve property " + name);
      }
      return stack.peek().getProperty(name.substring(1, name.length()));
    } else {
      return scope.getVariable(name);
    }
  }

  public boolean has(String name) {
    if (name.startsWith("@")) {
      if (self == null) {
        return false;
      } else {
        return self.hasProperty(name.substring(1, name.length()));
      }
    } else if (name.startsWith("#")) {
      if (stack == null || stack.isEmpty()) {
        return false;
      } else {
        return stack.peek().hasProperty(name.substring(1, name.length()));
      }
    } else {
      return scope.hasVariable(name);
    }
  }

  public void set(String name, LyteValue value) {
    set(name, value, false);
  }

  public void set(String name) {
    set(name, stack.pop());
  }

  public void set(String name, boolean localOnly) {
    if (localOnly) {
      scope.putLocalVariable(name, stack.pop(), false);
    } else {
      set(name, stack.pop());
    }
  }

  public void set(String name, LyteValue value, boolean finalize) {
    if (name.startsWith("@")) {
      if (self == null) {
        throw new LyteError("Cannot set property " + name);
      }
      self.setProperty(name.substring(1, name.length()), value);
    } else if (name.startsWith("#")) {
      if (stack == null || stack.isEmpty()) {
        throw new LyteError("Cannot set property " + name);
      }
      stack.peek().setProperty(name.substring(1, name.length()), value);
    } else {
      scope.putVariable(name, value, finalize);
    }
  }

  public LyteValue resolve(LyteInvokeStatement invokeStatement, boolean fullyResolve) {
    return resolve(invokeStatement, fullyResolve, true);
  }

  public LyteValue resolve(LyteInvokeStatement invokeStatement, boolean fullyResolve, boolean applyLast) {
    String primaryIdentifier = invokeStatement.getPrimaryIdentifier();
    PeekingIterator<LyteSpecifier> specifierIterator = invokeStatement.getSpecifiersIterator(fullyResolve ? 0 : 1);
    LyteValue obj, lastObj = primaryIdentifier.startsWith("#") ? stack.peek() : self;

    try {
      if (primaryIdentifier.equals("#") || primaryIdentifier.equals("@")) {
        obj = lastObj;
      } else {
        obj = get(primaryIdentifier);
      }

      if (shouldApply(specifierIterator, applyLast)) {
        obj = obj.apply(new LyteContext(lastObj, scope, stack));
      }

      if (!shouldApply(specifierIterator, applyLast)) {
        if (primaryIdentifier.startsWith("#")) {
          lastObj = stack.peek();
        } else if (primaryIdentifier.startsWith("@") || obj.is("block")) {
          lastObj = self;
        }
      } else {
        lastObj = obj;
      }

      while (specifierIterator.hasNext()) {
        LyteSpecifier specifier = specifierIterator.next();

        if (specifier.identifier != null) {
          obj = obj.getProperty(specifier.identifier);
        } else if (specifier.invokables != null) {
          obj = obj.getProperty(specifier.invokables.apply(this).toString());
        } else {
          // Add a clone of each of the arguments to the function
          for (int i = (specifier.arguments.size() - 1); i >= 0; i--) {
            stack.push(specifier.arguments.get(i).clone(this, true, true));
          }
          // Then invoke the block itself
          if (obj == LyteReflectionFunctions.reflectGet || obj == LyteReflectionFunctions.reflectEval) {
            obj = obj.apply(this);
          } else {
            obj = obj.apply(new LyteContext(lastObj, scope, stack));
          }
        }

        if (shouldApply(specifierIterator, applyLast) && specifier.arguments == null && obj != null) {
          lastObj = obj = obj.apply(new LyteContext(lastObj, scope, stack));
        }
      }
    } catch (LyteError e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new LyteError(e.getMessage());
    }

    return obj;
  }

  public LyteContext enter(LyteContext context, boolean shouldEnter, boolean hasSelf) {
    return new LyteContext(hasSelf ? self : context.self, shouldEnter ? scope.enter() : scope, context.stack);
  }

  private static boolean shouldApply(PeekingIterator<LyteSpecifier> specifierIterator, boolean applyLast) {
    return (!specifierIterator.hasNext() && applyLast) || (specifierIterator.hasNext() && (specifierIterator.peek().arguments == null));
  }

  @Override
  public void inject(String name, LyteValue value) {
    scope.inject(name, value);
  }
}
