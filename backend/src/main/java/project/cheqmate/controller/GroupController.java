package project.cheqmate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.*;
import project.cheqmate.model.Group;
import project.cheqmate.service.StorageService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final StorageService storage;

    public GroupController(StorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    public void createGroup(@RequestBody CreateGroupRequest req, Principal principal) {
        List<String> members = new ArrayList<>(req.getMemberNames());
        String creatorName = principal.getName();
        if (!members.contains(creatorName)) {
            members.add(creatorName);
        }
        storage.createGroupWithMembers(req.getGroupName(), members);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @GetMapping
    public List<Group> getGroups() {
        return storage.getGroups();
    }

    @GetMapping("/my")
    public List<GroupSummaryResponse> getMyGroups(Principal principal) {
        return storage.getGroupsByUser(principal.getName());
    }

//    @GetMapping("/{id}")
//    public Group getGroup(@PathVariable int id) {
//        return storage.getGroupById(id);
//    }

    @PostMapping("/{id}/members")
    public Group addMember(@PathVariable int id, @RequestBody AddMemberRequest req) {
        return storage.addUserToGroup(id, req.getUserName());
    }

    @PatchMapping("/{id}")
    public Group change_name(@PathVariable int id, @RequestBody RenameRequest req) {
        return storage.changeGroupName(id, req.getNewName());
    }

    @DeleteMapping("/{id}")
    public void deleteGroup(@PathVariable int id) {
        storage.deleteGroup(id);
    }

    @GetMapping("/{id}/cheques")
    public List<ChequeResponse> getGroupCheques(@PathVariable int id) {
        return storage.getChequesByGroupId(id);
    }
}