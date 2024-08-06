//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        CharStream input = CharStreams.fromFileName("ExampleProtocol.txt");
        ChoreoLexer lexer = new ChoreoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChoreoParser parser = new ChoreoParser(tokens);
        ParseTree tree = parser.choreo();
        System.out.println(new ChoreoGrammarVisitor().visit(tree));
    }
}