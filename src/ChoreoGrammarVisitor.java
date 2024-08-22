import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ChoreoGrammarVisitor extends ChoreoBaseVisitor<AST> {
    public static Environment env = new Environment();
    /* ---------- term ---------- */

    @Override
    public AST visitFunction(ChoreoParser.FunctionContext ctx) {
        Arguments args = (Arguments) visit(ctx.as);
        switch (ctx.f.getText()) {
            case "crypt":
                return new Crypt(args.getArguments().get(0), args.getArguments().get(1));
            case "pair":
        }
        return new Function(ctx.f.getText(), (Arguments) visit(ctx.as));
    }

    @Override
    public AST visitVariable(ChoreoParser.VariableContext ctx) {
        return new Variable(ctx.x.getText());
    }

    @Override
    public AST visitMAC(ChoreoParser.MACContext ctx) {
        return new MAC((Term) visit(ctx.m), (Term) visit(ctx.k));
    }

    @Override
    public AST visitTermParen(ChoreoParser.TermParenContext ctx) {
        return visit(ctx.m);
    }

    /* ---------- choreo ---------- */

    @Override
    public AST visitArguments(ChoreoParser.ArgumentsContext ctx) {
        List<Term> args = new ArrayList<>();
        for (ChoreoParser.TermContext arc : ctx.as) {
            args.add((Term) visit(arc));
        }
        return new Args(args);
    }

    /* ---------- choreo ---------- */

    @Override
    public AST visitEmpty(ChoreoParser.EmptyContext ctx) {
        return new Empty();
    }

    @Override
    public AST visitMessage(ChoreoParser.MessageContext ctx) {
        env.addAgent(ctx.a.getText());
        env.addAgent(ctx.b.getText());
        if (ctx.l == null) return new Message(ctx.a.getText(), ctx.b.getText(), (Choice) visit(ctx.ch));
        return new Message(ctx.a.getText(), ctx.b.getText(), ctx.l.getText(), (Choice) visit(ctx.ch));
    }

    @Override
    public AST visitDefinition(ChoreoParser.DefinitionContext ctx) {
        Definition def = new Definition(ctx.a.getText(), ctx.vars.stream().map(var -> new Variable(var.getText())).toList(), (Choreo) visit(ctx.c));
        Frame frame = new Frame(def, ctx.a.getText(), ctx.vars.stream().map(Token::getText).toList());
        env.addAgent(ctx.a.getText());
        env.addFrame(frame);
        return def;
    }

    @Override
    public AST visitChoreoParen(ChoreoParser.ChoreoParenContext ctx) {
        return visit(ctx.c);
    }

    /* ---------- cont ---------- */

    @Override
    public AST visitContinuation(ChoreoParser.ContinuationContext ctx) {
        if (ctx.c == null) return new Cont((Term) visit(ctx.t));
        return new Cont((Term) visit(ctx.t), (Choreo) visit(ctx.c));
    }

    /* ---------- choice ---------- */

    @Override
    public AST visitChoices(ChoreoParser.ChoicesContext ctx) {
        if (ctx.ch == null) return new Choice((Cont) visit(ctx.co));
        return new Choice((Cont) visit(ctx.co), (Choice) visit(ctx.ch));
    }

    @Override
    public AST visitChoicesParen(ChoreoParser.ChoicesParenContext ctx) {
        if (ctx.ch == null) return new Choice((Cont) visit(ctx.co));
        return new Choice((Cont) visit(ctx.co), (Choice) visit(ctx.ch));
    }
}
