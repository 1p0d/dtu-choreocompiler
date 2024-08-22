import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Environment {
    private List<Frame> frames;
    private List<String> agents;

    public Environment() {
        frames = new ArrayList<>();
        agents = new ArrayList<>();
    }

    public void addAgent(String agent) {
        if (agents.contains(agent)) return;
        agents.add(agent);
    }

    public List<String> getAgents() {
        return agents;
    }

    public void addFrame(Frame frame) {
        frames.add(frame);
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void compileAllFrames() {
        for (String agent : agents) {
            StringBuilder localCode = new StringBuilder();
            for (Frame frame : frames) {
                if (frame.getAgent().equals(agent)) {
                    localCode.append(frame.compile(this));
                }
            }
            // Write localCode to a file named "{agent}.local.choreo"
            writeToFile(agent + ".local.choreo", localCode.toString());
        }
    }

    private void writeToFile(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
