package project.cheqmate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.CreateChequeRequest;
import project.cheqmate.dto.FortuneWheelRequest;
import project.cheqmate.dto.RecognizeChequeRequest;
import project.cheqmate.model.Cheque;
import project.cheqmate.service.ChequeRecognizeService;
import project.cheqmate.service.StorageService;


@RestController
@RequestMapping("/api/cheques")
public class ChequeController {

    private final StorageService storage;
    private final ChequeRecognizeService recognizeService;

    public ChequeController(StorageService storage, ChequeRecognizeService recognizeService) {
        this.storage = storage;
        this.recognizeService = recognizeService;
    }

    @PostMapping
    public Cheque createCheque(@RequestBody CreateChequeRequest req) {
        // System.out.println(req.getTotal());
        return storage.createCheque(
                req.getGroupName(), req.getChequeName(), req.getTotal(),
                req.getOwnerName(), req.getWhoPaidName(), req.getProportions(),
                req.getItems());
    }



    @PostMapping("/fortune-wheel")
    public Cheque playFortuneWheel(@RequestBody FortuneWheelRequest req) {
        return storage.playFortuneWheel(
                req.getGroupName(), req.getChequeName(), req.getTotal(), req.getOwnerName());
    }

    @PostMapping({"/recognize"})
    public ResponseEntity<String> recognizeCheque(@RequestBody RecognizeChequeRequest req) {
        String chequeJson = recognizeService.callProverkaCheka(req.getQr());
        return ResponseEntity.ok(chequeJson);
    }

    @DeleteMapping("/{id}")
    public void deleteCheque(@PathVariable int id) {
        storage.deleteCheque(id);
    }
}
