package project.cheqmate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.CreateChequeRequest;
import project.cheqmate.dto.FortuneWheelRequest;
import project.cheqmate.dto.RecognizeChequeRequest;
import project.cheqmate.model.Cheque;
import project.cheqmate.model.User;
import project.cheqmate.repository.UserRepository;
import project.cheqmate.service.AchievementService;
import project.cheqmate.service.ChequeRecognizeService;
import project.cheqmate.service.StorageService;

@RestController
@RequestMapping("/api/cheques")
public class ChequeController {

    private final StorageService storage;
    private final ChequeRecognizeService recognizeService;
    private final AchievementService achievementService;
    private final UserRepository userRepo;

    public ChequeController(StorageService storage, ChequeRecognizeService recognizeService,
                            AchievementService achievementService, UserRepository userRepo) {
        this.storage = storage;
        this.recognizeService = recognizeService;
        this.achievementService = achievementService;
        this.userRepo = userRepo;
    }

    @PostMapping
    public Cheque createCheque(@RequestBody CreateChequeRequest req) {
        Cheque cheque = storage.createCheque(
                req.getGroupId(),
                req.getChequeName(),
                req.getOwnerName(),
                req.getWhoPaidName(),
                null,
                req.getItems()
        );
        userRepo.findByName(req.getOwnerName()).ifPresent(owner ->
                achievementService.onChequeSaved(owner, req.isFromQr()));
        if (!req.getOwnerName().equals(req.getWhoPaidName())) {
            userRepo.findByName(req.getWhoPaidName()).ifPresent(achievementService::onPaidForOthers);
        }
        if (req.isFromRoulette()) {
            userRepo.findByName(req.getWhoPaidName()).ifPresent(achievementService::onRouletteLoss);
        }
        return cheque;
    }

    @PostMapping("/fortune-wheel")
    public Cheque playFortuneWheel(@RequestBody FortuneWheelRequest req) {
        return storage.playFortuneWheel(
                req.getGroupId(),
                req.getChequeName(),
                req.getTotal(),
                req.getOwnerName()
        );
    }

    @PostMapping("/recognize")
    public ResponseEntity<String> recognizeCheque(@RequestBody RecognizeChequeRequest req) {
        String chequeJson = recognizeService.callProverkaCheka(req.getQr());
        return ResponseEntity.ok(chequeJson);
    }

    @DeleteMapping("/{id}")
    public void deleteCheque(@PathVariable int id) {
        storage.deleteCheque(id);
    }
}