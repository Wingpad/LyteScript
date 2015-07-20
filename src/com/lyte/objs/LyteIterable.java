package com.lyte.objs;

import com.lyte.objs.LyteBlock;
import com.lyte.objs.LyteValue;

import java.util.Iterator;

/**
 * Created by a0225785 on 7/20/2015.
 */
public interface LyteIterable {
  LyteBlock generator();
  Iterator<LyteValue> iterator();
}
