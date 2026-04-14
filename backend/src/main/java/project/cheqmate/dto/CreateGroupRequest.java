package project.cheqmate.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    private String groupName;
    private List<String> memberNames;
}
