package com.lyte.objs;


import com.lyte.stdlib.LyteStringFunctions;
import org.json.simple.JSONValue;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jszaday on 6/22/15.
 */
public class LyteString extends LytePrimitive<String> {

  private static final LyteStringFunctions STRING_FUNCTIONS = new LyteStringFunctions();

  public LyteString(String value) {
    super(value);
  }

  public LyteString(char value) {
    super(Character.toString(value));
  }

  public char charAt(int index) {
    return get().charAt(index);
  }

  public int length() {
    return get().length();
  }

  @Override
  public boolean toBoolean() {
    return get().length() > 0;
  }

  @Override
  public double toNumber() {
    try {
      return Double.parseDouble(get());
    } catch (NumberFormatException e) {
      throw new LyteError("Cannot cast string '" + get() + "' to double!");
    }
  }

  @Override
  public LyteValue getProperty(String property) {
    Integer index;
    if ((index = LyteList.tryParse(property)) != null) {
      try {
        return new LyteString(get().charAt(index));
      } catch (StringIndexOutOfBoundsException e) {
        throw new LyteError("Index out of bounds, " + index + "!");
      }
    } else if (STRING_FUNCTIONS.hasProperty(property)) {
      return STRING_FUNCTIONS.getProperty(property);
    } else {
      throw new LyteError("Cannot invoke the property " + property + " of a string.");
    }
  }

  @Override
  public boolean equals(LyteValue other) {
    if (other.is(typeOf())) {
      return equalsStrict(other);
    } else if (other.isSimpleComparison()) {
      return other.equals(this);
    } else {
      return other.toString().equals(this);
    }
  }
  
  @Override
  public boolean isSimpleComparison() {
    return true;
  }

  @Override
  public boolean hasProperty(String property) {
    return STRING_FUNCTIONS.hasProperty(property) || (LyteList.tryParse(property) != null);
  }

  @Override
  public Set<String> getProperties() {
    return new HashSet<String>() {{
      addAll(STRING_FUNCTIONS.getProperties());

      for (int i = 0; i < length(); i++) {
        add(String.valueOf(i));
      }
    }};
  }

  @Override
  public String toJSONString() {
    return JSONValue.toJSONString(get());
  }
}
