package com.lyte.utils;

import com.lyte.objs.LyteError;
import com.lyte.objs.LyteList;
import com.lyte.objs.LyteNumber;

/**
 * Created by jszaday on 7/13/2015.
 */
public class LyteRangeMaker {
  public static LyteList linspace(double start, double finish, double number) {
    double step;
    number = Math.floor(number) - 1;

    if (start > finish) {
      step = (start - finish) / number;
    } else {
      step = (finish - start) / number;
    }

    return range(start, step, finish);
  }

  public static LyteList range(double start, double finish) {
    return range(start, start > finish ? -1 : 1, finish);
  }

  public static LyteList range(double start, double step, double finish) {
    LyteList range = new LyteList();

    if (start != finish && step == 0) {
      throw new LyteError("Impossible to reach " + start + " from " + finish + " by incrementing by zero");
    } else if (start <= finish) {
      if (step < 0) {
        throw new LyteError("Impossible to reach " + start + " from " + finish + " by incrementing by " + step);
      }

      for (double i = start; i <= finish; i += step) {
        range.add(LyteNumber.valueOf(i));
      }
    } else {
      if (step > 0) {
        throw new LyteError("Impossible to reach " + start + " from " + finish + " by incrementing by " + step);
      }

      for (double i = start; i >= finish; i += step) {
        range.add(LyteNumber.valueOf(i));
      }
    }

    return range;
  }
}
