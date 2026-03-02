package project.cheqmate;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
public class Application {

    private final Scanner scanner;
    State state;
    ObjectMapper objectMapper;

    Application(){
        scanner = new Scanner(System.in);
        state = new State();
        objectMapper = new ObjectMapper();
    }

    public void run() throws IOException {

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
            System.out.println("7. Make json.");
            System.out.println("8. Load from json.");
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
                    make_json(); // почему он тут?
                    running = false;
                    break;
                case 7:
                    make_json();
                    break;
                case 8:
                    load_from_json_menu();
                    break;
                default:
                    System.out.println("Unknown command.");
            }

        }
        scanner.close();
    }

    public void createUser() throws IOException {
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Enter the username:");

        String username = scanner.nextLine();
        User user = new User(username);
        state.addUser(user);

        System.out.println("You created the user.");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
    }

    public void createGroup() throws IOException{
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Enter the name of the group:");

        String groupName = scanner.nextLine();

        Group group = new Group(groupName);

        state.addGroup(group);

        System.out.println("Whom to add to the group? Type the number of people:");

        int numberOfPeople = scanner.nextInt();
        scanner.nextLine(); // пропуск /n

        System.out.println("Type the " + Integer.toString(numberOfPeople) + " names:");

        for (int i = 0; i < numberOfPeople; i++) {
            String name = scanner.nextLine();
            add_info(name, "group", groupName);
            group.addMember(findUserByName(name));
        }

        System.out.println("You created the group.");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
    }

    public void addCheque() throws IOException{
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Add this cheque to which group? Type the group's name:");

        String groupString = scanner.nextLine();
        Group group = findGroupByName(groupString); // мб тут будет null

        System.out.println("Enter the name of the cheque:");
        String chequeName = scanner.nextLine();

        System.out.println("Enter the owner of the cheque:");
        String chequeOwnerString = scanner.nextLine();
        User chequeOwner = findUserByName(chequeOwnerString); // мб тут будет null
        add_info(chequeOwnerString, "cheque_owner", chequeName);

        System.out.println("Enter the user who paid:");
        String whoPaidString = scanner.nextLine();
        User whoPaid = findUserByName(whoPaidString); // мб тут будет null
        add_info(chequeOwnerString, "cheque_paid", chequeName);

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
            String name = scanner.next();
            User user = findUserByName(name);
            double value = scanner.nextDouble();
            scanner.nextLine(); // пропуск /n
            String info = "name of cheque: " + chequeName + ", debt: " + Double.toString(value * total / 100) + ", paid by: " + whoPaidString;
            add_info(name, "cheque", info);
            cheque.addUser(user, value);
        }

        System.out.println("You created the cheque in the group \"" + groupString + "\".");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
    }

    public void writeDebts() throws IOException {
        for (User user: state.getUsers()) {
            System.out.println(user.getName() + "'s debtors:");
            for(Map.Entry<User, Double> entry : user.getDebtors().entrySet()) {
                String nameDebtor = entry.getKey().getName();
                double amount = entry.getValue();
                System.out.println("    " + nameDebtor + " - " + Double.toString(amount));
                String info = "name of debtor: " + nameDebtor + ", debt: " + Double.toString(amount);
                add_info(user.getName(), "debtors", info);
            }
            System.out.println(user.getName() + "'s creditors:");
            for(Map.Entry<User, Double> entry : user.getCreditors().entrySet()) {
                String nameCreditor = entry.getKey().getName();
                double amount = entry.getValue();
                System.out.println("    " + nameCreditor + " - " + Double.toString(amount));
                String info = "name of creditor: " + nameCreditor + ", debt: " + Double.toString(amount);
                add_info(user.getName(), "creditors", info);
            }
        }
    }

    private User findUserByName(String targetString) throws  IOException {
        User target = null;
        List<User> users = state.getUsers();
        for (User user: users) {
            if (Objects.equals(user.getName(), targetString)) {
                target = user;
            }
        }
        if (target == null) {
            System.out.println("There is no user with that name.");
            // придумать что делать дальше
            System.out.println("Please create user");
            createUser();
            target = users.get(users.size() - 1);
        }
        return target;
    }

    private Group findGroupByName(String targetString) {
        Group target = null;
        List<Group> groups = state.getGroups();
        for (Group group: groups) {
            if (Objects.equals(group.getGroupName(), targetString)) {
                target = group;
            }
        }
        if (target == null) {
            System.out.println("There is no group with that name.");
            // придумать что делать дальше
            target = new Group(targetString);
            groups.add(target);
        }
        return target;
    }

    private void printAll() {
        System.out.println("Users:");
        for (User user: state.getUsers()) {
            System.out.println(user.getName());
        }
        System.out.println("Groups:");
        for (Group group: state.getGroups()) {
            System.out.println(group.getGroupName());
        }
    }

    private void make_json() throws IOException {
        for (User us : state.getUsers()) {
            File dir = new File("src/main/java/user_files/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File user_file = new File(dir, "file_" + us.getName() + ".json");
            user_file.createNewFile();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(user_file, us.getInfo());
        }
        System.out.println("You have created json");
    }

    private void load_from_json_menu() throws IOException {
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Enter the path to the JSON file:");
        String filePath = scanner.nextLine();
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            parse_json(file);
            System.out.println("Data loaded from " + filePath);
        } else {
            System.out.println("File not found: " + filePath);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
    }

    private void add_info(String name, String key, String info) throws IOException {
        User user = findUserByName(name);
        if (user.getInfo().containsKey(key)) {
            user.getInfo().get(key).add(info);
        } else {
            ArrayList<String> list = new ArrayList<>();
            list.add(info);
            user.getInfo().put(key, list);
        }
    }

    void load_from_json(String[] files) throws IOException {
        for (String file_path : files) {
            File path = new File(file_path);
            parse_json(path);
        }
    }

    private void parse_json(File json_file) throws IOException {
        LinkedHashMap<String, ArrayList<String>> loadedInfo = objectMapper.readValue(json_file, new TypeReference<>() {});
        ArrayList<String> names = loadedInfo.get("name");
        if (names == null || names.isEmpty()) {
            System.out.println("Invalid JSON: 'name' field is missing or empty.");
            return;
        }
        String userName = names.get(0);
        
        User user = null;
        List<User> users = state.getUsers();
        for (User u : users) {
            if (u.getName().equals(userName)) {
                user = u;
                break;
            }
        }
        
        if (user == null) {
            user = new User(userName);
            users.add(user);
        }

        user.getInfo().putAll(loadedInfo);
        ArrayList<String> groupNames = loadedInfo.get("group");
        if (groupNames != null) {
            for (String groupName : groupNames) {
                Group group = findGroupByName(groupName);
                group.addMember(user);
            }
        }
    }
}
