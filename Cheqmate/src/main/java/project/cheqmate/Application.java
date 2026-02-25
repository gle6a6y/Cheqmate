package project.cheqmate;

import java.util.*;

public class Application {

    private final Scanner scanner;
    List<User> users; // можно сделать мапу <имя чела, User>, чтоб быстро по имени можно было найти
    List<Group> groups; // аналогично

    Application(){
        scanner = new Scanner(System.in);
        users = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public void run() {

        boolean running = true;

        while (running) {
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("1. Create user.");
            System.out.println("2. Create group.");
            System.out.println("3. Add a cheque to the group.");
            // System.out.println("4. Add a user to the group"); // пока не нужно, так как можно при создании группы добавлять
            System.out.println("4. Write the debts.");
            System.out.println("5. Print users and groups.");
            System.out.println("6. Exit.");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            int instructionNumber = scanner.nextInt();
            scanner.nextLine(); // пропуск /n

            switch (instructionNumber) {
                case 1:
                    createUser();
                    break;
                case 2:
                    createGroup();
                    break;
                case 3:
                    addCheque();
                    break;
                case 4:
                    writeDebts();
                    break;
                case 5:
                    printAll();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Unknown command.");
            }

        }
        scanner.close();
    }

    public void createUser() {
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Enter the username:");

        String username = scanner.nextLine();
        User user = new User(username);
        users.add(user);

        System.out.println("You created the user.");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
    }

    public void createGroup() {
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Enter the name of the group:");

        String groupName = scanner.nextLine();

        Group group = new Group(groupName);

        groups.add(group);

        System.out.println("Whom to add to the group? Type the number of people:");

        int numberOfPeople = scanner.nextInt();
        scanner.nextLine(); // пропуск /n

        System.out.println("Type the " + Integer.toString(numberOfPeople) + " names:");

        for (int i = 0; i < numberOfPeople; i++) {
            String name = scanner.nextLine();
            User user = new User(name);
            group.addMember(user);
        }

        System.out.println("You created the group.");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
    }

    public void addCheque() {
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Add this cheque to which group? Type the group's name:");

        String groupString = scanner.nextLine();
        Group group = findGroupByName(groupString); // мб тут будет null

        System.out.println("Enter the name of the cheque:");
        String chequeName = scanner.nextLine();

        System.out.println("Enter the owner of the cheque:");
        String chequeOwnerString = scanner.nextLine();
        User chequeOwner = findUserByName(chequeOwnerString); // мб тут будет null

        System.out.println("Enter the user who paid:");
        String whoPaidString = scanner.nextLine();
        User whoPaid = findUserByName(whoPaidString); // мб тут будет null

        System.out.println("What is the cost of the cheque?");
        double total = scanner.nextDouble();
        scanner.nextLine(); // пропуск /n

        Cheque cheque = new Cheque(chequeName, total, chequeOwner, whoPaid);
        group.addCheque(cheque);

        System.out.println("Whom to add to the group? Enter the number of people:");
        int numberOfPeople = scanner.nextInt();
        scanner.nextLine(); // пропуск /n

        System.out.println("Enter the " + Integer.toString(numberOfPeople) + " names of the users and how many percent of the total amount they owe:");
        for (int i = 0; i < numberOfPeople; i++) {
            User user = findUserByName(scanner.next());
            double value = scanner.nextDouble();
            scanner.nextLine(); // пропуск /n
            cheque.addUser(user, value);
        }

        System.out.println("You created the cheque in the group \"" + groupString + "\".");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
    }

    public void writeDebts() {
        for (User user: users) {
            System.out.println(user.getName() + "'s debtors:");
            for(Map.Entry<User, Double> entry : user.getDebtors().entrySet()) {
                String nameDebtor = entry.getKey().getName();
                double amount = entry.getValue();
                System.out.println("    " + nameDebtor + " - " + Double.toString(amount));
            }
            System.out.println(user.getName() + "'s creditors:");
            for(Map.Entry<User, Double> entry : user.getCreditors().entrySet()) {
                String nameCreditor = entry.getKey().getName();
                double amount = entry.getValue();
                System.out.println("    " + nameCreditor + " - " + Double.toString(amount));
            }
        }
    }

    private User findUserByName(String targetString) {
        User target = null;
        for (User user: users) {
            if (Objects.equals(user.getName(), targetString)) {
                target = user;
            }
        }
        if (target == null) {
            System.out.println("There is no user with that name.");
            // придумать что делать дальше
        }
        return target;
    }

    private Group findGroupByName(String targetString) {
        Group target = null;
        for (Group group: groups) {
            if (Objects.equals(group.getGroupName(), targetString)) {
                target = group;
            }
        }
        if (target == null) {
            System.out.println("There is no group with that name.");
            // придумать что делать дальше
        }
        return target;
    }

    private void printAll() {
        System.out.println("Users:");
        for (User user: users) {
            System.out.println(user.getName());
        }
        System.out.println("Groups:");
        for (Group group: groups) {
            System.out.println(group.getGroupName());
        }
    }
}
