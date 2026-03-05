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

    private final StorageService storage;
    private final Scanner scanner = new Scanner(System.in);

    public Application(StorageService storage) {
        this.storage = storage;
    }

    @Override
    public void run(String... args) {
        boolean running = true;
        while (running) {
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("1. Create user.");
            System.out.println("2. Create group.");
            System.out.println("3. Add a cheque to the group.");
            System.out.println("4. Write the debts.");
            System.out.println("5. Print users and groups.");
            System.out.println("6. Exit.");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            int cmd = scanner.nextInt();
            scanner.nextLine();

            switch (cmd) {
                case 1 -> promptCreateUser();
                case 2 -> promptCreateGroup();
                case 3 -> promptAddCheque();
                case 4 -> writeDebts();
                case 5 -> printAll();
                case 6 -> running = false;
                default -> System.out.println("Unknown command.");
            }
        }
        scanner.close();
    }

    private void promptCreateUser() {
        System.out.println("Enter the username:");
        String name = scanner.nextLine();
        storage.createUser(name);
        System.out.println("You created the user.");
    }

    private void promptCreateGroup() {
        System.out.println("Enter the name of the group:");
        String groupName = scanner.nextLine();
        storage.createGroup(groupName);

        System.out.println("How many people to add?");
        int n = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Type the " + n + " names:");
        for (int i = 0; i < n; i++) {
            String name = scanner.nextLine();
            User user = storage.getUserByName(name);
            if (user == null) {
                System.out.println("User not found, creating: " + name);
                storage.createUser(name);
            }
            storage.addUserToGroupByName(groupName, name);
        }
        System.out.println("You created the group.");
    }

    private void promptAddCheque() {
        System.out.println("Add this cheque to which group? Type the group's name:");
        String groupName = scanner.nextLine();

        System.out.println("Enter the name of the cheque:");
        String chequeName = scanner.nextLine();

        System.out.println("Enter the owner of the cheque:");
        String ownerName = scanner.nextLine();

        System.out.println("Enter the user who paid:");
        String whoPaidName = scanner.nextLine();

        System.out.println("What is the cost of the cheque?");
        double total = scanner.nextDouble();
        scanner.nextLine();

        Cheque cheque = storage.createCheque(groupName, chequeName, total, ownerName, whoPaidName);

        System.out.println("How many people to split with? Enter the number:");
        int n = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Enter name and percent for each person:");
        for (int i = 0; i < n; i++) {
            String name = scanner.next();
            double percent = scanner.nextDouble();
            scanner.nextLine();

            User user = storage.getUserByName(name);
            if (user != null) {
                storage.addUserToCheque(cheque.getId(), user.getId(), percent);
            }
        }
        storage.applyCheque(cheque.getId());
        System.out.println("Cheque created in group \"" + groupName + "\".");
    }

    private void writeDebts() {
        for (User user : storage.getUsers()) {
            var debts = storage.getDebts(user.getId());
            System.out.println(user.getName() + "'s debtors:");
            for (var d : debts.getOrDefault("debtors", List.of())) {
                System.out.println("    " + d.get("name") + " - " + d.get("amount"));
            }
            System.out.println(user.getName() + "'s creditors:");
            for (var c : debts.getOrDefault("creditors", List.of())) {
                System.out.println("    " + c.get("name") + " - " + c.get("amount"));
            }
        }
    }

    private void printAll() {
        System.out.println("Users:");
        for (User u : storage.getUsers()) System.out.println("  " + u.getName());
        System.out.println("Groups:");
        for (Group g : storage.getGroups()) System.out.println("  " + g.getGroupName());
    }
}
