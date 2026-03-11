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
        return storage.createCheque(
                req.getGroupName(), req.getChequeName(), req.getTotal(),
                req.getOwnerName(), req.getWhoPaidName(), req.getProportions());
    }

    @DeleteMapping("/{id}")
    public void deleteCheque(@PathVariable int id) {
        storage.deleteCheque(id);
    }
}
