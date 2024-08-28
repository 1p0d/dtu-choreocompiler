import java.util.ArrayList;
import java.util.List;

public class ChoreoGrammarVisitor extends ChoreoBaseVisitor<AST> {
    public static Environment env = new Environment();

    /* ---------- start ---------- */

    public AST visitStart(ChoreoParser.StartContext ctx) {
        visit(ctx.knwl);
        return visit(ctx.c);
    }

    /* ---------- knwl ---------- */

    public AST visitKnowledge(ChoreoParser.KnowledgeContext ctx) {
        String agent = ctx.a.getText();
        env.agents.add(agent);
        List<Term> knowledge = new ArrayList<>();
        for (ChoreoParser.TermContext c : ctx.ts) {
            knowledge.add((Term) visit(c));
        }
        env.frames.add(new Frame(knowledge));
        return null;
    }

    /* ---------- term ---------- */

    @Override
    public AST visitFunction(ChoreoParser.FunctionContext ctx) {
        List<Term> args = new ArrayList<>();
        for (ChoreoParser.TermContext arc : ctx.as) {
            args.add((Term) visit(arc));
        }
        return new Function(ctx.f.getText(), args);
    }

    @Override
    public AST visitVariable(ChoreoParser.VariableContext ctx) {
        return new Variable(ctx.x.getText());
    }

    @Override
    public AST visitMAC(ChoreoParser.MACContext ctx) {
        List<Term> args = new ArrayList<>();
        args.add((Term) visit(ctx.m));
        args.add((Term) visit(ctx.k));
        return new Function(RegisteredFunction.MAC.name, args);
    }

    @Override
    public AST visitTermParen(ChoreoParser.TermParenContext ctx) {
        return visit(ctx.m);
    }

    /* ---------- choreo ---------- */

    @Override
    public AST visitEmpty(ChoreoParser.EmptyContext ctx) {
        return new Empty();
    }

    @Override
    public AST visitMessage(ChoreoParser.MessageContext ctx) {
        List<Choice> choices = new ArrayList<>();
        for (ChoreoParser.ChoiceContext ch : ctx.chs) {
            choices.add((Choice) visit(ch));
        }
        if (ctx.l == null) return new Message(ctx.a.getText(), ctx.b.getText(), choices);
        return new Message(ctx.a.getText(), ctx.b.getText(), choices, ctx.l.getText());
    }

    @Override
    public AST visitDefinition(ChoreoParser.DefinitionContext ctx) {
        return new Definition(ctx.a.getText(), ctx.vars.stream().map(var -> new Variable(var.getText())).toList(), (Choreo) visit(ctx.c));
    }

    @Override
    public AST visitChoreoParen(ChoreoParser.ChoreoParenContext ctx) {
        return visit(ctx.c);
    }

    /* ---------- choice ---------- */

    @Override
    public AST visitContinuation(ChoreoParser.ContinuationContext ctx) {
        if (ctx.c == null) return new Continuation((Term) visit(ctx.t));
        return new Continuation((Term) visit(ctx.c), (Choreo) visit(ctx.c));
    }

    @Override
    public AST visitChoiceParen(ChoreoParser.ChoiceParenContext ctx) {
        return visit(ctx.ch);
    }
}
