package project.cheqmate;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Application runner = new Application();
        if (args.length > 0) {
            runner.load_from_json(args);
        }
        runner.run();
    }
}