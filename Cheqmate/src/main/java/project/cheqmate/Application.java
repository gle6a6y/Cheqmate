package project.cheqmate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import project.cheqmate.model.*;
import project.cheqmate.model.Group;
import project.cheqmate.model.User;
import project.cheqmate.service.StorageService;

import java.util.*;

@Component
@ConditionalOnProperty(name = "cheqmate.cli", havingValue = "true")
public class Application implements CommandLineRunner {

//    private final StorageService storage;
      private final Cli cli;

//    public Application(StorageService storage) {
//        this.storage = storage;
//        cli = new Cli(storage);
//    }

    public Application(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void run(String... args) {
        cli.cli_run();
    }

}
