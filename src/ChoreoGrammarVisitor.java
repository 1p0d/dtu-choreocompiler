import java.util.ArrayList;
import java.util.List;

public class ChoreoGrammarVisitor extends ChoreoBaseVisitor<AST> {
    public static Environment env = new Environment();

    /* ---------- start ---------- */

    public AST visitStart(ChoreoParser.StartContext ctx) {
        Choreo choreo = (Choreo) visit(ctx.c);
        List<Knowledge> knowledges = new ArrayList<>();
        ctx.ks.forEach(k -> knowledges.add((Knowledge) visit(k)));
        return new Start(choreo, knowledges);
    }

    /* ---------- knwl ---------- */

    public AST visitKnowledge(ChoreoParser.KnowledgeContext ctx) {
        String agent = ctx.a.getText();
        List<Term> terms = new ArrayList<>();
        for (ChoreoParser.TermContext c : ctx.ts) {
            terms.add((Term) visit(c));
        }
        return new Knowledge(agent, terms);
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
    public AST visitConstant(ChoreoParser.ConstantContext ctx) {
        return new Constant(ctx.x.getText());
    }

    @Override
    public AST visitMAC(ChoreoParser.MACContext ctx) {
        List<Term> args = new ArrayList<>();
        args.add((Term) visit(ctx.k));
        args.add((Term) visit(ctx.m));
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
        return new Definition(ctx.a.getText(), ctx.vars.stream().map(var -> new Constant(var.getText())).toList(), (Choreo) visit(ctx.c));
    }

    @Override
    public AST visitChoreoParen(ChoreoParser.ChoreoParenContext ctx) {
        return visit(ctx.c);
    }

    /* ---------- choice ---------- */

    @Override
    public AST visitContinuation(ChoreoParser.ContinuationContext ctx) {
        if (ctx.c == null) return new Choice((Term) visit(ctx.t));
        return new Choice((Term) visit(ctx.t), (Choreo) visit(ctx.c));
    }

    @Override
    public AST visitChoiceParen(ChoreoParser.ChoiceParenContext ctx) {
        return visit(ctx.ch);
    }
}
