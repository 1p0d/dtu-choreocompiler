import java.util.ArrayList;
import java.util.List;

public class ChoreoGrammarVisitor extends ChoreoBaseVisitor<AST> {
    public static Environment env = new Environment();

    /* ---------- start ---------- */

    public AST visitStart(ChoreoParser.StartContext ctx) {
        if (ctx.ks == null || ctx.c == null) AST.error("Grammar rule start violated.");
        Choreo choreo = (Choreo) visit(ctx.c);
        List<Knowledge> knowledges = new ArrayList<>();
        ctx.ks.forEach(k -> knowledges.add((Knowledge) visit(k)));
        return new Start(choreo, knowledges);
    }

    /* ---------- knwl ---------- */

    public AST visitKnowledge(ChoreoParser.KnowledgeContext ctx) {
        if (ctx.a == null || ctx.ts == null) AST.error("Grammar rule Knowledge violated.");
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
        if (ctx.f == null || ctx.as == null) AST.error("Grammar rule Function violated.");
        List<Term> args = new ArrayList<>();
        for (ChoreoParser.TermContext arc : ctx.as) {
            args.add((Term) visit(arc));
        }
        return new Function(ctx.f.getText(), args);
    }

    @Override
    public AST visitConstant(ChoreoParser.ConstantContext ctx) {
        if (ctx.x == null) AST.error("Grammar rule Constant violated.");
        return new Constant(ctx.x.getText());
    }

    @Override
    public AST visitMAC(ChoreoParser.MACContext ctx) {
        if (ctx.k == null || ctx.m == null) AST.error("Grammar rule MAC violated.");
        Term key = (Term) visit(ctx.k);
        Term message = (Term) visit(ctx.m);
        return new Function(RegisteredFunction.PAIR.name, List.of(message,
                new Function(RegisteredFunction.MAC.name, List.of(key, message))));
    }

    @Override
    public AST visitTermParen(ChoreoParser.TermParenContext ctx) {
        if (ctx.m == null) AST.error("Grammar rule TermParen violated.");
        return visit(ctx.m);
    }

    /* ---------- choreo ---------- */

    @Override
    public AST visitEmpty(ChoreoParser.EmptyContext ctx) {
        return new Empty();
    }

    @Override
    public AST visitMessage(ChoreoParser.MessageContext ctx) {
        if (ctx.a == null || ctx.b == null || ctx.chs == null) AST.error("Grammar rule Message violated.");
        List<Choice> choices = new ArrayList<>();
        for (ChoreoParser.ChoiceContext ch : ctx.chs)
            choices.add((Choice) visit(ch));
        if (ctx.l == null) return new Message(ctx.a.getText(), ctx.b.getText(), choices);
        return new Message(ctx.a.getText(), ctx.b.getText(), choices, ctx.l.getText());
    }

    @Override
    public AST visitDefinition(ChoreoParser.DefinitionContext ctx) {
        if (ctx.a == null || ctx.vars == null || ctx.c == null) AST.error("Grammar rule Definition violated.");
        return new Definition(ctx.a.getText(), ctx.vars.stream()
                .map(var -> new Constant(var.getText())).toList(), (Choreo) visit(ctx.c));
    }

    @Override
    public AST visitChoreoParen(ChoreoParser.ChoreoParenContext ctx) {
        if (ctx.c == null) AST.error("Grammar rule ChoreoParen violated.");
        return visit(ctx.c);
    }

    /* ---------- choice ---------- */

    @Override
    public AST visitContinuation(ChoreoParser.ContinuationContext ctx) {
        if (ctx.t == null || ctx.c == null) AST.error("Grammar rule Continuation violated.");
        return new Choice((Term) visit(ctx.t), (Choreo) visit(ctx.c));
    }

    @Override
    public AST visitChoiceParen(ChoreoParser.ChoiceParenContext ctx) {
        if (ctx.ch == null) AST.error("Grammar rule ChoiceParen violated.");
        return visit(ctx.ch);
    }
}
