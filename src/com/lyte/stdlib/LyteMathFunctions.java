package com.lyte.stdlib;

import com.lyte.core.LyteContext;
import com.lyte.objs.LyteError;
import com.lyte.objs.LyteList;
import com.lyte.objs.LyteNumber;

/**
 * Created by a0225785 on 7/1/2015.
 */
public class LyteMathFunctions {
  public static LyteNativeBlock mathLessEquals = new LyteNativeBlock("Math", "LessEquals", "<=") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val2 <= val1);
    }
  };

  public static LyteNativeBlock mathLess = new LyteNativeBlock("Math", "Less", "<") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val2 < val1);
    }
  };

  public static LyteNativeBlock mathGreaterEquals = new LyteNativeBlock("Math", "GreaterEquals", ">=") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val2 >= val1);
    }
  };

  public static LyteNativeBlock mathGreater = new LyteNativeBlock("Math", "Greater", ">") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val2 > val1);
    }
  };

  public static LyteNativeBlock mathAdd = new LyteNativeBlock("Math", "Add", "+") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val1 + val2);
    }
  };

  public static LyteNativeBlock mathSubtract = new LyteNativeBlock("Math", "Subtract", "-") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val2 - val1);
    }
  };

  public static LyteNativeBlock mathDivide = new LyteNativeBlock("Math", "Divide", "/") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val2 / val1);
    }
  };

  public static LyteNativeBlock mathIDivide = new LyteNativeBlock("Math", "IDivide", "IDiv") {

    @Override
    public void invoke(LyteContext context) {
      long val1 = (long) context.apply().toNumber();
      long val2 = (long) context.apply().toNumber();
      context.push(val2 / val1);
    }
  };

  public static LyteNativeBlock mathModulus = new LyteNativeBlock("Math", "Modulus", "Mod") {

    @Override
    public void invoke(LyteContext context) {
      long val1 = (long) context.apply().toNumber();
      long val2 = (long) context.apply().toNumber();
      context.push(val2 % val1);
    }
  };

  public static LyteNativeBlock mathMultiply = new LyteNativeBlock("Math", "Multiply", "*") {

    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(val1 * val2);
    }
  };

  public static LyteNativeBlock mathPow = new LyteNativeBlock("Math", "Pow", "^") {
    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(Math.pow(val2, val1));
    }
  };

  public static LyteNativeBlock mathMax = new LyteNativeBlock("Math", "Max", null) {
    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(Math.max(val2, val1));
    }
  };

  public static LyteNativeBlock mathMin = new LyteNativeBlock("Math", "Min", null) {
    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      context.push(Math.min(val2, val1));
    }
  };

  public static LyteNativeBlock mathRange2 = new LyteNativeBlock("Math", "Range2") {
    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      LyteList range = new LyteList();

      if (val1 <= val2) {
        for (double i = val1; i <= val2; i += 1) {
          range.add(LyteNumber.valueOf(i));
        }
      } else {
        for (double i = val1; i >= val2; i -= 1) {
          range.add(LyteNumber.valueOf(i));
        }
      }

      context.push(range);
    }
  };

  public static LyteNativeBlock mathRange3 = new LyteNativeBlock("Math", "Range3") {
    @Override
    public void invoke(LyteContext context) {
      double val1 = context.apply().toNumber();
      double val3 = context.apply().toNumber();
      double val2 = context.apply().toNumber();
      LyteList range = new LyteList();

      if (val1 != val2 && val3 == 0) {
        throw new LyteError("Impossible to reach " + val2 + " from " + val1 + " by incrementing by zero");
      }

      if (val1 <= val2) {
        if (val3 < 0) {
          throw new LyteError("Impossible to reach " + val2 + " from " + val1 + " by incrementing by " + val3);
        }

        for (double i = val1; i <= val2; i += val3) {
          range.add(LyteNumber.valueOf(i));
        }
      } else {
        if (val3 > 0) {
          throw new LyteError("Impossible to reach " + val2 + " from " + val1 + " by incrementing by " + val3);
        }

        for (double i = val1; i >= val2; i += val3) {
          range.add(LyteNumber.valueOf(i));
        }
      }

      context.push(range);
    }
  };

  public static LyteNativeBlock mathNaN = new LyteNativeBlock("Math", "NaN") {
    @Override
    public void invoke(LyteContext context) {
      context.push(Double.NaN);
    }
  };

  public static LyteNativeBlock mathIsNaN = new LyteNativeBlock("Math", "IsNaN", "NaN?") {
    @Override
    public void invoke(LyteContext context) {
      Double val1 = context.apply().toNumber();
      context.push(val1.isNaN());
    }
  };

  public static LyteNativeBlock mathPositiveInf = new LyteNativeBlock("Math", "PositiveInfinity", "Inf") {
    @Override
    public void invoke(LyteContext context) {
      context.push(Double.POSITIVE_INFINITY);
    }
  };

  public static LyteNativeBlock mathNegativeInf = new LyteNativeBlock("Math", "NegativeInfinity", "-Inf") {
    @Override
    public void invoke(LyteContext context) {
      context.push(Double.NEGATIVE_INFINITY);
    }
  };

  public static LyteNativeBlock mathIsInfinite = new LyteNativeBlock("Math", "IsInfinite", "Inf?") {
    @Override
    public void invoke(LyteContext context) {
      Double val1 = context.apply().toNumber();
      context.push(val1.isInfinite());
    }
  };

  public static LyteNativeBlock mathSqrt = new LyteNativeBlock("Math", "Sqrt", null) {
    @Override
    public void invoke(LyteContext context) {
      context.push(Math.sqrt(context.apply().toNumber()));
    }
  };
}
