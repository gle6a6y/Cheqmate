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
        return storage.createGroupWithMembers(req.getGroupName(), req.getMemberNames());
    }

    @GetMapping
    public List<Group> getGroups() {
        return storage.getGroups();
    }

    @GetMapping("/{id}")
    public Group getGroup(@PathVariable int id) {
        return storage.getGroupById(id);
    }

    @PostMapping("/{id}/members")
    public Group addMember(@PathVariable int id, @RequestBody AddMemberRequest req) {
        return storage.addUserToGroup(id, req.getUserName());
    }

    @PatchMapping("/{id}")
    public Group change_name(@PathVariable int id, @RequestBody RenameRequest req) {
        return storage.changeGroupName(id, req.getNewName());
    }

    @DeleteMapping("/{id}")
    public Group deleteGroup(@PathVariable int id) {
        return storage.deleteGroup(id);
    }
}