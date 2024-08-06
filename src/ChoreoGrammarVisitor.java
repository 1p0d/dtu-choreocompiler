import org.antlr.v4.runtime.Token;

import java.util.List;

public class ChoreoGrammarVisitor extends ChoreoBaseVisitor<String> {
    /* ---------- term ---------- */

    @Override
    public String visitFunction(ChoreoParser.FunctionContext ctx) {
        return ctx.f.getText() + "(" + visit(ctx.as) + ")";
    }

    @Override
    public String visitVariable(ChoreoParser.VariableContext ctx) {
        return ctx.x.getText();
    }

    @Override
    public String visitMAC(ChoreoParser.MACContext ctx) {
        return "[" + visit(ctx.m) + "]" + visit(ctx.k);
    }

    @Override
    public String visitTermParen(ChoreoParser.TermParenContext ctx) {
        return "(" + visit(ctx.m) + ")";
    }

    /* ---------- choreo ---------- */

    @Override
    public String visitArguments(ChoreoParser.ArgumentsContext ctx) {
        List<String> args = ctx.as.stream().map(this::visit).toList();
        return String.join(",", args);
    }

    /* ---------- choreo ---------- */

    @Override
    public String visitEmpty(ChoreoParser.EmptyContext ctx) {
        return "0";
    }

    @Override
    public String visitMessage(ChoreoParser.MessageContext ctx) {
        return ctx.a.getText() + " -> " + ctx.b.getText() + ": " + (ctx.l != null ? "(" + ctx.l.getText() + ") " : "") + visit(ctx.ch);
    }

    @Override
    public String visitDefinition(ChoreoParser.DefinitionContext ctx) {
        List<String> vars = ctx.vars.stream().map(Token::getText).toList();
        return ctx.a.getText() + ": new " + String.join(",", vars) + ". " + visit(ctx.c);
    }

    @Override
    public String visitChoreoParen(ChoreoParser.ChoreoParenContext ctx) {
        return "(" + visit(ctx.c) + ")";
    }

    /* ---------- cont ---------- */

    @Override
    public String visitContinuation(ChoreoParser.ContinuationContext ctx) {
        return visit(ctx.t) + (ctx.c != null ? ". " + visit(ctx.c) : "");
    }

    /* ---------- choice ---------- */

    @Override
    public String visitChoices(ChoreoParser.ChoicesContext ctx) {
        return visit(ctx.co) + (ctx.ch != null ? " + " + visit(ctx.ch) : "");
    }

    @Override
    public String visitChoicesParen(ChoreoParser.ChoicesParenContext ctx) {
        return "(" + visit(ctx.co) + (ctx.ch != null ? " + " + visit(ctx.ch) : "") + ")";
    }
}
