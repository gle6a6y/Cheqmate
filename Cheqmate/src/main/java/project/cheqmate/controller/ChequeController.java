package project.cheqmate.controller;

import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.CreateChequeRequest;
import project.cheqmate.model.Cheque;
import project.cheqmate.service.StorageService;

import java.util.Map;

@RestController
@RequestMapping("/api/cheques")
public class ChequeController {

    private final StorageService storage;

    public ChequeController(StorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    public Cheque createCheque(@RequestBody CreateChequeRequest req) {
        Cheque cheque = storage.createCheque(
                req.getGroupName(), req.getChequeName(), req.getTotal(),
                req.getOwnerName(), req.getWhoPaidName());

        if (req.getProportions() != null) {
            for (Map.Entry<String, Double> entry : req.getProportions().entrySet()) {
                var user = storage.getUserByName(entry.getKey());
                if (user != null) {
                    storage.addUserToCheque(cheque.getId(), user.getId(), entry.getValue());
                }
            }
            storage.applyCheque(cheque.getId());
        }

        return cheque;
    }
}
