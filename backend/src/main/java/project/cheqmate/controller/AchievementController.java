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
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final UserRepository userRepository;

    private static final Map<String, String[]> META = Map.of(
            AchievementService.FIRST_CHEQUE, new String[]{"\uD83E\uDDFE Первый чек", "Создал первый чек"},
            AchievementService.GENEROUS,     new String[]{"\uD83E\uDD11 Щедрый", "Оплатил за других 5 раз"},
            AchievementService.HONEST,       new String[]{"\uD83D\uDE07 Честный", "Погасил долг"},
            AchievementService.LOSER,        new String[]{"\uD83D\uDCC9 Неудачник", "Проиграл рулетку"},
            AchievementService.SCANNER,      new String[]{"\uD83D\uDCF7 Сканер", "Создал чек через QR"},
            AchievementService.BIG_SPENDER,  new String[]{"\uD83D\uDCB0 Транжира", "Личные расходы превысили 10 000 ₽"}
    );

    @GetMapping("/my")
    public ResponseEntity<List<AchievementResponse>> myAchievements(Principal principal) {
        User user = userRepository.findByName(principal.getName()).orElseThrow();
        List<AchievementResponse> result = achievementService.getAchievements(user).stream()
                .map(a -> {
                    String[] meta = META.getOrDefault(a.getAchievementKey(),
                            new String[]{a.getAchievementKey(), ""});
                    return new AchievementResponse(a.getAchievementKey(), meta[0], meta[1], a.getUnlockedAt());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
