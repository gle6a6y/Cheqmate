package project.cheqmate.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.AddMemberRequest;
import project.cheqmate.dto.CreateGroupRequest;
import project.cheqmate.dto.RenameRequest;
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
    @Transactional(readOnly = true)
    public List<Group> getGroups() {
        return storage.getGroups();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Group getGroup(@PathVariable int id) {
        List<Group> groups = getGroups();
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    @PostMapping("/{id}/members")
    @Transactional
    public Group addMember(@PathVariable int id, @RequestBody AddMemberRequest req) {
        Group group = storage.getGroups().stream()
                .filter(g -> g.getId().equals(id)).findFirst().orElseThrow();
        return storage.addUserToGroupByName(group.getGroupName(), req.getUserName());
    }

    @PatchMapping("/{id}")
    @Transactional
    public Group change_name(@PathVariable int id, @RequestBody RenameRequest req) {
        return storage.changeGroupName(id, req.getNewName());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteGroup(@PathVariable int id) {
        storage.deleteGroup(id);
    }
}