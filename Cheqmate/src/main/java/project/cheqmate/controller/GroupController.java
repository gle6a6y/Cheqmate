package project.cheqmate.controller;

import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.AddMemberRequest;
import project.cheqmate.dto.CreateGroupRequest;
import project.cheqmate.model.Group;
import project.cheqmate.service.StorageService;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final StorageService storage;

    public GroupController(StorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    public Group createGroup(@RequestBody CreateGroupRequest req) {
        Group group = storage.createGroup(req.getGroupName());
        if (req.getMemberNames() != null) {
            for (String name : req.getMemberNames()) {
                storage.addUserToGroupByName(group.getGroupName(), name);
            }
        }
        return storage.getGroupByName(group.getGroupName());
    }

    @GetMapping
    public List<Group> getGroups() {
        return storage.getGroups();
    }

    @PostMapping("/{id}/members")
    public Group addMember(@PathVariable int id, @RequestBody AddMemberRequest req) {
        Group group = storage.getGroups().stream()
                .filter(g -> g.getId().equals(id)).findFirst().orElseThrow();
        return storage.addUserToGroupByName(group.getGroupName(), req.getUserName());
    }
}
