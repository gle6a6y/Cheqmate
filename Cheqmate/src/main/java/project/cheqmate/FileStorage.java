package project.cheqmate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class FileStorage {
    File path;
    File stateFile;
    ObjectMapper objectMapper;

    FileStorage() {
        path = new File("src/main/java/state/");
        if (!path.exists()) {
            path.mkdir();
        }
        stateFile = new File(path, "state.json");
        objectMapper = new ObjectMapper();
    }

    public void loadState(State state) throws IOException {
        JsonNode root = objectMapper.readTree(stateFile);
        JsonNode users = root.get("users");
        for(JsonNode user : users) {
            int id = user.get("id").asInt();
            String name = user.get("name").asText();

            User newUser = new User(id, name);
            state.addUser(newUser);
            state.setNumberOfUsers(id + 1);
        }
        JsonNode groups = root.get("groups");
        for(JsonNode group : groups) {

            String groupName = group.get("groupName").asText();
            Group newGroup = new Group(groupName);
            state.addGroup(newGroup);

            JsonNode members = group.get("members");

            for(JsonNode member : members) {
                int id = member.get("id").asInt();
                newGroup.addMember(state.getUserById(id));
            }

            JsonNode cheques = group.get("cheques");
            for(JsonNode cheque : cheques) {
                String name = cheque.get("name").asText();
                double total = cheque.get("total").asDouble();
                int ownerId = cheque.get("ownerId").asInt();
                int whoPaidId = cheque.get("whoPaidId").asInt();
                Cheque newCheque = new Cheque(name, total, ownerId, whoPaidId);

                JsonNode proportions = cheque.get("proportions");
                Iterator<String> fieldNames = proportions.fieldNames();
                while(fieldNames.hasNext()) {
                    String key = fieldNames.next();
                    int userId = Integer.parseInt(key);
                    double percent = proportions.get(key).asDouble();
                    newCheque.addUser(userId, percent);
                }
                state.applyCheque(newCheque);
            }
        }
    }

    public void saveState(State state) throws IOException {
        LinkedHashMap<String, Object> info = new LinkedHashMap<>();
        info.put("users", state.getUsers());
        info.put("groups", state.getGroups());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(stateFile, info);
    }
}
