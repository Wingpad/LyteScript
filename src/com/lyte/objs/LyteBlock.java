package com.lyte.objs;

import com.lyte.core.LyteContext;
import com.lyte.core.LyteStack;
import com.lyte.core.LyteStatement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jszaday on 6/17/2015.
 */
public class LyteBlock extends LytePrimitive<List<LyteStatement>> {

  private List<String> mArgs;
  protected LyteContext mContext;
  private boolean mCanEnterScope;
  private final boolean mHasSelf, mMaintainsObjs;
  private LinkedList<LyteValue> mObjContexts;

  public LyteBlock(LyteContext context, List<LyteStatement> statements) {
    this(context, statements, null, true, false, true);
  }

  public LyteBlock(LyteContext context, List<LyteStatement> statements, List<String> args, boolean canEnter, boolean hasSelf) {
    this(context, statements, args, canEnter, hasSelf, true);
  }

  public LyteBlock(LyteContext context, List<LyteStatement> statements, List<String> args, boolean canEnter, boolean hasSelf, boolean maintainsObjs) {
    super(statements);
    mArgs = args;
    mCanEnterScope = canEnter;
    mContext = context;
    mObjContexts = new LinkedList<>();
    mMaintainsObjs = maintainsObjs;

    if (mHasSelf = hasSelf && mMaintainsObjs) {
      mObjContexts.push(context.self);
    }
  }

  private void popArgs(LyteContext context) {
    if (mArgs == null) {
      return;
    }
    // For each of our args
    for (String arg : mArgs) {
      // Pop off a value and bind it to the arg's name
      // Verify this occurs only in our scope
      context.set(arg, true);
    }
  }

  public void invoke(LyteContext context) {
    LyteStatement statement = null;
    // Enter a new scope
    context = mContext.enter(context, mCanEnterScope, mObjContexts.peek());
    // Pop any named arguments
    popArgs(context);
    // Then apply each of our statements to our scope
    Iterator<LyteStatement> statementIterator = get().iterator();
    while (statementIterator.hasNext()) {
      statement = statementIterator.next();
      try {
        statement.applyTo(context);
      } catch (LyteError e) {
        e.addLineNumber(statement.getLineNumber());
        throw e;
      }
    }
    // Pop any expired object context
    popObjContext();
  }

  @Override
  public String typeOf() {
    return "block";
  }

  @Override
  public String toString() {
    return (mArgs == null ? "[]" : mArgs) + " => " + get();
  }

  @Override
  public boolean toBoolean() {
    return true;
  }

  @Override
  public double toNumber() {
    return get().size();
  }

  @Override
  public LyteValue<List<LyteStatement>> clone(LyteContext context) {
    return new LyteBlock(context, get(), mArgs, true, false);
  }

  public LyteValue<List<LyteStatement>> clone(LyteContext context, boolean canEnter, boolean hasSelf) {
    return new LyteBlock(context, get(), mArgs, canEnter, hasSelf);
  }

  @Override
  public LyteValue apply(LyteContext context) {
    LyteStack stack = context.stack;
    int origStackSize = stack.size();
    invoke(context);
    if ((stack.size() - origStackSize) > 1) {
      throw new LyteError("Error Applying Block, " + this + ", expected 1 return value instead found " + (stack.size() - origStackSize) + "!");
    } else if (!stack.isEmpty()) {
      return stack.pop();
    } else {
      return null;
    }
  }

  @Override
  public String toJSONString() {
    throw new LyteError("Cannot encode a block as JSON!");
  }

  public void pushObjContext(LyteValue value) {
    if (!mMaintainsObjs) {
      return;
    }

    if (value != null && !(hasObjContexts() && value.equals(mObjContexts.peek()))) {
      mObjContexts.push(value);
    }
  }

  protected LyteValue popObjContext() {
    if (mObjContexts.isEmpty()) {
      return null;
    } else if (mHasSelf && mObjContexts.size() == 1) {
      return mObjContexts.peek();
    } else {
      return mObjContexts.pop();
    }
  }

  protected boolean hasObjContexts() {
    return !mObjContexts.isEmpty();
  }
}
