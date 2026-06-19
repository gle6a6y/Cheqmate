package project.cheqmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cheqmate.dto.AchievementResponse;
import project.cheqmate.model.User;
import project.cheqmate.repository.UserRepository;
import project.cheqmate.service.AchievementService;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<List<AchievementResponse>> myAchievements(Principal principal) {
        User user = userRepository.findByName(principal.getName()).orElseThrow();
        List<AchievementResponse> result = achievementService.getAchievements(user).stream()
                .map(a -> {
                    String[] meta = AchievementService.META.getOrDefault(a.getAchievementKey(),
                            new String[]{a.getAchievementKey(), ""});
                    return new AchievementResponse(a.getAchievementKey(), meta[0], meta[1], a.getUnlockedAt());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
