package com.lyte.core;

import com.lyte.gen.LyteBaseVisitor;
import com.lyte.gen.LyteLexer;
import com.lyte.gen.LyteParser;
import com.lyte.objs.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by a0225785 on 7/2/2015.
 */
public class LyteCompiler extends LyteBaseVisitor<Object> {

  private LyteRawBlock mGlobalBlock;
  private LyteRawBlock mCurrentBlock;
  private CommonTokenStream mTokenStream;

  public LyteRawBlock compile(String program) {
    try {
      return compile(new ANTLRInputStream(new StringReader(program)));
    } catch (IOException e) {
      throw new LyteError(e.getMessage());
    }
  }

  public LyteRawBlock compile(CharStream charStream) {
    // Reinitialize the Blocks
    mCurrentBlock = mGlobalBlock = LyteRawBlock.newGlobal();
    // Setup our parser w/ the correct input
    LyteLexer lexer = new LyteLexer(charStream);
    mTokenStream = new CommonTokenStream(lexer);
    LyteParser parser = new LyteParser(mTokenStream);
    // Then grab the parse tree and begin to visit it
    ParseTree parseTree = parser.program();
    this.visit(parseTree);
    // Return the Global Block
    return mGlobalBlock;
  }

  @Override
  public Object visitStatement(LyteParser.StatementContext ctx) {
    // Add the newly generated statement to the current block
    mCurrentBlock.addStatement((LyteStatement) visitChildren(ctx));
    // And return nothing
    return null;
  }

  @Override
  public Object visitSimpleStatement(LyteParser.SimpleStatementContext ctx) {
    LyteStatement statement;
    // If it is a pushable
    if (ctx.pushable() != null) {
      // use the pushable statement visitor to handle it accordingly
      statement = (LyteStatement) visitPushableStatement(ctx.pushable());
    } else {
      // otherwise, simply get the statement as a result from visiting the kids
      statement = (LyteStatement) visitChildren(ctx);
    }
    return statement;
  }

  public Object visitPushableStatement(LyteParser.PushableContext ctx) {
    // If this is an invokable expression
    if (ctx.invokable() != null) {
      // Handle it accordingly
      return visitInvokeStatement(ctx.invokable());
    } else {
      // Otherwise, visit the kids and generate a statement from the result
      return new LytePushStatement(getLineNumber(ctx), (LyteValue) visitChildren(ctx));
    }
  }

  @Override
  public Object visitLambdaExpression(LyteParser.LambdaExpressionContext ctx) {
    List<TerminalNode> nodes = ctx.lambdaArgsList().Identifier();
    mCurrentBlock = mCurrentBlock.enter();
    for (TerminalNode node : nodes) {
      mCurrentBlock.addArg(node.getText());
    }
    return visitBlock(ctx.block(), false);
  }

  @Override
  public Object visitObjectLiteral(LyteParser.ObjectLiteralContext ctx) {
    LyteRawObject rawObject = new LyteRawObject();
    if (ctx.keyValueList() != null) {
      List<LyteParser.KeyValuePairContext> keyValueList = ctx.keyValueList().keyValuePair();
      // Visit each of the properties
      for (LyteParser.KeyValuePairContext keyValue : keyValueList) {
        // Adding it to the raw object as it goes
        rawObject.set(keyValue.Identifier().getText(), (LyteStatement) visitPushableStatement(keyValue.pushable()));
      }
    }
    return rawObject;
  }

  @Override
  public Object visitRange(LyteParser.RangeContext ctx) {
    LyteRawBlock block = mCurrentBlock.enter();

    for (int i = (ctx.numericLiteral().size() - 1); i >= 0; i--) {
      block.addStatement(new LytePushStatement(getLineNumber(ctx), (LyteValue) visitNumericLiteral(ctx.numericLiteral(i))));
    }

    block.addStatement(new LyteInvokeStatement(getLineNumber(ctx), "Range" + ctx.numericLiteral().size()));

    return block;
  }

  @Override
  public Object visitArrayLiteral(LyteParser.ArrayLiteralContext ctx) {
    LyteRawArray array = new LyteRawArray();
    LyteParser.ValueListContext valueList = ctx.valueList();
    if (valueList != null) {
      for (LyteParser.PushableContext pushable : valueList.pushable()) {
        array.add((LyteStatement) visitPushableStatement(pushable));
      }
    }
    return array;
  }

  @Override
  public Object visitNumericLiteral(LyteParser.NumericLiteralContext ctx) {
    String number = ctx.getText();
    int radix;
    // Decide on the radix and get rid of any extraneous characters
    if (ctx.BinaryIntegerLiteral() != null) {
      radix = 2;
      number = number.substring(2, number.length());
    } else if (ctx.OctalIntegerLiteral() != null) {
      radix = 8;
    } else if (ctx.HexIntegerLiteral() != null) {
      radix = 16;
      number = number.substring(2, number.length());
    } else {
      // Unless it is simply a decimal value, at which point just parse it directly
      return new LyteNumber(number);
    }
    // Otherwise, parse it as an integer with the determined radix
    return new LyteNumber(number, radix);
  }

  @Override
  public Object visitStringLiteral(LyteParser.StringLiteralContext ctx) {
    String string = ctx.getText();
    // Grab the String w/o Quotes
    string = string.substring(1, string.length() - 1);
    // And then Unescape it
    string = StringEscapeUtils.unescapeEcmaScript(string);
    // Finally, return a Lyte String created from it
    return new LyteString(string);
  }

  @Override
  public Object visitBlock(LyteParser.BlockContext ctx) {
    return visitBlock(ctx, true);
  }

  public Object visitBlock(LyteParser.BlockContext ctx, boolean enterNewBlock) {
    LyteRawBlock block;
    // If we have enter a new block, do so
    if (enterNewBlock) {
      mCurrentBlock = mCurrentBlock.enter();
    }
    // Visit the kids
    visitChildren(ctx);
    // Then leave
    block = mCurrentBlock;
    mCurrentBlock = mCurrentBlock.leave();
    // Returning the newly populated block
    return block;
  }

  public Object visitInvokeStatement(LyteParser.InvokableContext ctx) {
    // If there is only one child
    String primaryIdentifier = ctx.Identifier().getText();
    if (ctx.getChildCount() == 1) {
      // Simply return a simple invokable
      return new LyteInvokeStatement(getLineNumber(ctx), primaryIdentifier);
    } else {
      // Otherwise, construct the list of designators
      List<LyteInvokeStatement.LyteSpecifier> designators = new ArrayList<LyteInvokeStatement.LyteSpecifier>();
      LyteInvokeStatement.LyteSpecifier designator;

      for (int i = 1; i < ctx.getChildCount(); i++) {
        if ((designator = (LyteInvokeStatement.LyteSpecifier) visit(ctx.getChild(i))) != null) {
          designators.add(designator);
        }
      }

      return new LyteInvokeStatement(getLineNumber(ctx), primaryIdentifier, designators);
    }
  }

  public Object visitBindStatement(LyteParser.InvokableContext ctx) {
    return new LyteBindStatement(getLineNumber(ctx), (LyteInvokeStatement) visitInvokeStatement(ctx));
  }

  @Override
  public Object visitParameters(LyteParser.ParametersContext ctx) {
    if (ctx.parameterList() != null) {
      return visitParameterList(ctx.parameterList());
    } else {
      return null;
    }
  }

  @Override
  public Object visitParameterList(LyteParser.ParameterListContext ctx) {
    List<LyteRawBlock> parameters = new ArrayList<LyteRawBlock>();
    ParseTree child;
    mCurrentBlock = mCurrentBlock.enter();
    for (int i = 0; i < ctx.getChildCount(); i++) {
      child = ctx.getChild(i);
      // If we hit a comma
      if (child.getText().equals(",")) {
        // Add the simplified block to the parameters list
        parameters.add(mCurrentBlock.simplify());
        // Leave back to the parent scope and re-enter a new child
        mCurrentBlock = mCurrentBlock.leave().enter();
      } else {
        // Otherwise, add it by visiting the kid
        mCurrentBlock.addStatement((LyteStatement) visit(child));
      }
    }
    // Add the final block was to the parameters
    parameters.add(mCurrentBlock.simplify());
    // Then leave it
    mCurrentBlock = mCurrentBlock.leave();
    // And return a designator object
    return new LyteInvokeStatement.LyteSpecifier(parameters);
  }

  @Override
  public Object visitDesignator(LyteParser.DesignatorContext ctx) {
    if (ctx.Identifier() != null) {
      return new LyteInvokeStatement.LyteSpecifier(ctx.Identifier().toString());
    } else {
      // Enter a new block
      LyteRawBlock block = mCurrentBlock.enter();
      // Adding each of the "simple" statements to the list
      for (LyteParser.SimpleStatementContext simpleStatementContext : ctx.simpleStatement()) {
        block.addStatement((LyteStatement) visitSimpleStatement(simpleStatementContext));
      }
      // Then return a new specifier with the block as its contents
      return new LyteInvokeStatement.LyteSpecifier(block);
    }
  }

  @Override
  public Object visitInfixExpression(LyteParser.InfixExpressionContext ctx) {
    // First, visit the infix expression's statement, adding it to the current block
    mCurrentBlock.addStatement((LyteStatement) visitSimpleStatement(ctx.simpleStatement()));
    // Then, visit the invokable
    return visitInvokeStatement(ctx.invokable());
  }

  @Override
  public Object visitLeftBindingExpression(LyteParser.LeftBindingExpressionContext ctx) {
    // Visit the pushable
    mCurrentBlock.addStatement((LyteStatement) visitPushableStatement(ctx.pushable()));
    // Then visit the invokable destination
    return visitBindStatement(ctx.invokable());
  }

  @Override
  public Object visitRightBindingExpression(LyteParser.RightBindingExpressionContext ctx) {
    if (ctx.invokable() != null) {
      return visitBindStatement(ctx.invokable());
    } else {
      ArrayList<LyteInvokeStatement> statements = new ArrayList<>();
      for (LyteParser.InvokableContext invokableContext : ctx.invokableList().invokable()) {
        statements.add((LyteInvokeStatement) visitInvokeStatement(invokableContext));
      }
      return new LyteBindStatement(getLineNumber(ctx), statements);
    }
  }

  private String getLineNumber(ParserRuleContext ctx) {
    Token token = mTokenStream.get(ctx.getSourceInterval().a);
    return token.getLine() + ":" + token.getCharPositionInLine();
  }
}
