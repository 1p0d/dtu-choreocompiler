import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        CharStream input = CharStreams.fromFileName("input.choreo");
        ChoreoLexer lexer = new ChoreoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChoreoParser parser = new ChoreoParser(tokens);
        ParseTree tree = parser.start();
        ChoreoGrammarVisitor visitor = new ChoreoGrammarVisitor();
        Start start = (Start) visitor.visit(tree);
        Environment env = ChoreoGrammarVisitor.env;
        Map<String, List<Pair<Frame, Choreo>>> agentPairsMap = new HashMap<>();
        start.knowledges.forEach(knowledge -> agentPairsMap.put(knowledge.agent, List.of(new Pair<>(new Frame(knowledge.knowledge), start.choreo))));
        Map<String, String> agentTranslations = env.compile(agentPairsMap);
        for (String agent : agentTranslations.keySet()) {
            System.out.println("Local behavior for agent " + agent + ":");
            System.out.println(agentTranslations.get(agent));
            System.out.println();
        }
    }
}