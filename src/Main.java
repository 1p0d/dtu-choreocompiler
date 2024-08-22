import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        CharStream input = CharStreams.fromFileName("input.choreo");
        ChoreoLexer lexer = new ChoreoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChoreoParser parser = new ChoreoParser(tokens);
        ParseTree tree = parser.choreo();
        ChoreoGrammarVisitor visitor = new ChoreoGrammarVisitor();
        AST ast = visitor.visit(tree);
        Environment env = ChoreoGrammarVisitor.env;
        env.compileAllFrames();
        //System.out.println(((Choreo) ast).compile(env));
    }
}