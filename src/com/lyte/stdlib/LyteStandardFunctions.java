package com.lyte.stdlib;

import com.lyte.core.LyteInvokeStatement;
import com.lyte.core.LyteScope;
import com.lyte.core.LyteStack;
import com.lyte.objs.*;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by jszaday on 6/22/15.
 */
public class LyteStandardFunctions {

    public static String TOP_LEVEL_NAMESPACE = "Lyte";

    public static LyteNativeBlock coreTrue = new LyteNativeBlock("Core", "True") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            stack.push(true);
        }
    };

    public static LyteNativeBlock coreFalse = new LyteNativeBlock("Core", "False") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            stack.push(false);

        }
    };

    public static LyteNativeBlock coreUndefined = new LyteNativeBlock("Core", "Undefined") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            stack.push(LyteUndefined.UNDEFINED);

        }
    };

    public static LyteNativeBlock coreNot = new LyteNativeBlock("Core", "Not") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            stack.push(!stack.pop().apply(self, stack).toBoolean());
        }
    };

    public static LyteNativeBlock coreIsUndefined = new LyteNativeBlock("Core", "IsUndefined", "Undefined?") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            stack.push(stack.pop() == LyteUndefined.UNDEFINED);
        }
    };

    public static LyteNativeBlock mathAdd = new LyteNativeBlock("Math", "Add", "+") {

        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            double val1 = stack.pop().apply(self, stack).toNumber();
            double val2 = stack.pop().apply(self, stack).toNumber();
            stack.push(val1 + val2);
        }
    };

    public static LyteNativeBlock mathLessEquals = new LyteNativeBlock("Math", "LessEquals", "<=") {

        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            double val1 = stack.pop().apply(self, stack).toNumber();
            double val2 = stack.pop().apply(self, stack).toNumber();
            stack.push(val2 <= val1);
        }
    };

    public static LyteNativeBlock mathSubtract = new LyteNativeBlock("Math", "Subtract", "-") {

        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            double val1 = stack.pop().apply(self, stack).toNumber();
            double val2 = stack.pop().apply(self, stack).toNumber();
            stack.push(val2 - val1);
        }
    };

    public static LyteNativeBlock mathMultiply = new LyteNativeBlock("Math", "Multiply", "*") {

        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            double val1 = stack.pop().apply(self, stack).toNumber();
            double val2 = stack.pop().apply(self, stack).toNumber();
            stack.push(val1 * val2);
        }
    };

    public static LyteNativeBlock coreApply = new LyteNativeBlock("Core", "Apply") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value = stack.pop();

            if (value.typeOf().equals("block")) {
                ((LyteBlock) value).invoke(self, stack);
            } else {
                stack.push(value);
            }
        }
    };

    public static LyteNativeBlock coreConcatenate = new LyteNativeBlock("Core", "Concatenate", "++") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value2 = stack.pop().apply(self, stack);
            LyteValue value1 = stack.pop().apply(self, stack);

            // TODO Implement other cases
            if (value1.typeOf().equals("string") || value2.typeOf().equals("string")) {
                stack.push(value1.toString() + value2.toString());
            }
        }
    };

    public static LyteNativeBlock ioEchoLn = new LyteNativeBlock("IO", "EchoLn") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            if (!stack.isEmpty()) {
                System.out.println(stack.pop().apply(self, stack));
            } else {
                System.out.println();
            }
        }
    };

    public static LyteNativeBlock ioEcho = new LyteNativeBlock("IO", "Echo") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            System.out.print(stack.pop().apply(self, stack));
        }
    };

    public static LyteNativeBlock coreIf = new LyteNativeBlock("Core", "If") {

        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue condition = stack.pop();
            LyteValue trueValue = stack.pop();
            LyteValue falseValue = stack.pop();
            LyteValue selectedValue = condition.apply(self, stack).toBoolean() ? trueValue : falseValue;

            if (selectedValue.typeOf().equals("block")) {
                ((LyteBlock) selectedValue).invoke(self, stack);
            } else {
                stack.push(selectedValue.apply(self, stack));
            }
        }
    };

    public static LyteNativeBlock utilInstantiate = new LyteNativeBlock("Util", "Instantiate") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            // TODO this assumes the two things are on the same stack, we have to move scoping out of blocks!!!
            LyteValue value = stack.pop().apply(self, stack);
            if (!value.typeOf().equals("object")) {
                throw new LyteError("Cannot Instantiate a(n) " + value.typeOf() + ".");
            }
            if (!value.hasProperty("__constructor")) {
                throw new LyteError("Error, object has no constructor!");
            }
            LyteObject obj = (LyteObject) value.clone(null);
            try {
                ((LyteBlock) obj.getProperty("__constructor")).invoke(obj, stack);
            } catch (ClassCastException e) {
                throw new LyteError("Expected __constructor to be a block for object " + obj);
            }
            obj.unsetProperty("__constructor");
            stack.push(obj);
        }
    };

    public static LyteNativeBlock coreMixWith = new LyteNativeBlock("Core", "MixWith") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value2 = stack.pop();
            LyteValue value1 = stack.pop();

            if (!(value1.typeOf().equals("object") && value2.typeOf().equals("object"))) {
                throw new LyteError("Cannot mix " + value1 + " with " + value2);
            }

            ((LyteObject) value1).mixWith((LyteObject) value2);
        }
    };

    public static LyteNativeBlock errorTry = new LyteNativeBlock("Error", "Try") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value1 = stack.pop();
            LyteValue value2 = stack.pop();

            if (!value1.typeOf().equals("block") || !value2.typeOf().equals("block")) {
                throw new LyteError("Try requires both parameters to be blocks!");
            }

            try {
                ((LyteBlock) value1).invoke(self, stack);
            } catch (LyteError e) {
                stack.push(e);
                ((LyteBlock) value2).invoke(self, stack);
            }
        }
    };

    public static LyteNativeBlock errorRaise = new LyteNativeBlock("Error", "Raise") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            // Throw the result to the wolves
            throw new LyteError(stack.pop().apply(self, stack));
        }
    };

    public static LyteNativeBlock coreSwap = new LyteNativeBlock("Core", "Swap") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value1 = stack.pop();
            LyteValue value2 = stack.pop();
            stack.push(value1);
            stack.push(value2);
        }
    };

    public static LyteNativeBlock coreFor = new LyteNativeBlock("Core", "For") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value1 = stack.pop().apply(self, stack);
            LyteValue value2;

            if (value1.typeOf().equals("list")) {
                value2 = stack.pop();

                if (!value2.typeOf().equals("block")) {
                    throw new LyteError("For expected a block, not a(n) " + value2.typeOf());
                }

                for (LyteValue value : ((List<LyteValue>) value1.get())) {
                    // Push the value onto the stack
                    stack.push(value);
                    // Then invoke the function
                    ((LyteBlock) value2).invoke(self, stack);
                }
            } else {
                int number1 = (int) value1.toNumber();
                int number2 = (int) stack.pop().apply(self, stack).toNumber();
                value2 = stack.pop();
                if (!value2.typeOf().equals("block")) {
                    throw new LyteError("For expected a block, not a(n) " + value2.typeOf());
                }
                if (number1 < number2) {
                    for (int i = number1; i < number2; i++) {
                        // Push the number onto the stack
                        stack.push(i);
                        // Then invoke the function
                        ((LyteBlock) value2).invoke(self, stack);
                    }
                } else {
                    for (int i = (number1 - 1); i >= number2; i--) {
                        // Push the number onto the stack
                        stack.push(i);
                        // Then invoke the function
                        ((LyteBlock) value2).invoke(self, stack);
                    }
                }
            }
        }
    };

    public static LyteNativeBlock coreWhile = new LyteNativeBlock("Core", "While") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value1 = stack.pop();
            LyteValue value2 = stack.pop();
            if (!value2.typeOf().equals("block")) {
                throw new LyteError("While expected a block, not a(n) " + value2.typeOf());
            }
            while (value1.apply(self, stack).toBoolean()) {
                ((LyteBlock) value2).invoke(self, stack);
            }
        }
    };

    public static LyteNativeBlock coreUntil = new LyteNativeBlock("Core", "Until") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            LyteValue value1 = stack.pop();
            LyteValue value2 = stack.pop();
            if (!value2.typeOf().equals("block")) {
                throw new LyteError("Until expected a block, not a(n) " + value2.typeOf());
            }
            while (!value1.apply(self, stack).toBoolean()) {
                ((LyteBlock) value2).invoke(self, stack);
            }
        }
    };

    public static LyteNativeBlock coreIsStackEmpty = new LyteNativeBlock("Core", "IsStackEmpty", "StackEmpty?") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            stack.push(stack.isEmpty());
        }
    };

    public static LyteNativeBlock coreThis = new LyteNativeBlock("Core", "This") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            if (self != null) {
                stack.push(self);
            } else {
                stack.push(LyteUndefined.NULL);
            }
        }
    };

    public static LyteNativeBlock mathPow = new LyteNativeBlock("Math", "Pow", "**") {
        @Override
        public void invoke(LyteValue self, LyteStack stack) {
            double val1 = stack.pop().apply(self, stack).toNumber();
            double val2 = stack.pop().apply(self, stack).toNumber();
            stack.push(Math.pow(val2, val1));
        }
    };
}
