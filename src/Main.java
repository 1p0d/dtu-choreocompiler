import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) throw new IllegalArgumentException("Missing arguments: Please provide a path to the choreography file!");
        Path path = Paths.get(args[0]);
        CharStream input = CharStreams.fromStream(Files.newInputStream(path));
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
            String fileName = agent + ".local.choreo";
            FileWriter writer = new FileWriter(fileName);
            writer.write(agentTranslations.get(agent));
            writer.close();
            System.out.println("Created " + Path.of(System.getProperty("user.dir"), fileName));
        }
    }
}