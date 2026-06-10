package project.cheqmate.service;

import org.springframework.stereotype.Service;
import project.cheqmate.model.Debt;
import project.cheqmate.model.Group;
import project.cheqmate.model.User;
import project.cheqmate.repository.DebtRepository;
import project.cheqmate.repository.UserRepository;

import java.util.*;

@Service
public class DebtOptimizationService {
    private final UserRepository userRepo;
    private final DebtRepository debtRepo;

    public DebtOptimizationService(UserRepository userRepo, DebtRepository debtRepo) {
        this.userRepo = userRepo;
        this.debtRepo = debtRepo;
    }

    public void optimize(Group group) {
        List<Debt> debts = debtRepo.findByGroupIdAndStatus(
                group.getId(), Debt.Status.OPEN);

        if (debts.isEmpty()) {
            return;
        }

        Set<Integer> userIds = new HashSet<>();

        for(Debt debt : debts) {
            userIds.add(debt.getDebtor().getId());
            userIds.add(debt.getCreditor().getId());
        }

        if(userIds.size() <= 20) {
            List<User> users = userRepo.findAllById(userIds);
            Map<Integer, User> userCache = new HashMap<>();
            for (User u : users) {
                userCache.put(u.getId(), u);
            }

            List<Debt> bestDebts = dfsOptimize(debts, userIds, group, userCache);

            for (Debt old : debts) {
                old.setStatus(Debt.Status.SUPERSEDED);
            }
            debtRepo.saveAll(debts);
            debtRepo.saveAll(bestDebts);
        } else {
            greedyOptimize();
        }
    }

    private static class BestResult {
        int minTransactions;
        List<Debt> bestTransactions;
    }

    private void dfs(int pos, double[] debt, int[] idxToUserIdx, List<Debt> cur,
                     BestResult best, List<Integer> idList, Group group, Map<Integer, User> userCache) {
        int m = debt.length;

        while (pos < m && Math.abs(debt[pos]) < 1e-6) {
            pos++;
        }

        if (pos == m) {
            if (cur.size() < best.minTransactions) {
                best.minTransactions = cur.size();
                best.bestTransactions = new ArrayList<>(cur);
            }
            return;
        }

        if (cur.size() >= best.minTransactions) {
            return;
        }

        double current = debt[pos];

        for (int j = pos + 1; j < m; j++) {
            if (Math.abs(debt[j]) < 1e-6) continue;
            if (current * debt[j] > 0) continue;

            double x = Math.min(Math.abs(current), Math.abs(debt[j]));

            int fromIdx, toIdx;
            if (current < 0 && debt[j] > 0) {
                fromIdx = pos;
                toIdx = j;
            } else if (current > 0 && debt[j] < 0) {
                fromIdx = j;
                toIdx = pos;
            } else {
                continue;
            }

            debt[pos] += (fromIdx == pos ? x : -x);
            debt[j] += (fromIdx == j ? x : -x);

            int fromUserId = idList.get(idxToUserIdx[fromIdx]);
            int toUserId = idList.get(idxToUserIdx[toIdx]);

            User creditor = userCache.get(toUserId);
            User debtor = userCache.get(fromUserId);
            cur.add(new Debt(creditor, debtor, group, x));

            dfs(pos, debt, idxToUserIdx, cur, best, idList, group, userCache);

            cur.remove(cur.size() - 1);
            debt[pos] -= (fromIdx == pos ? x : -x);
            debt[j] -= (fromIdx == j ? x : -x);
        }
    }

    private List<Debt> dfsOptimize(List<Debt> debts, Set<Integer> userIds, Group group, Map<Integer, User> userCache) {
        List<Integer> idList = new ArrayList<>(userIds);
        Map<Integer, Integer> idToIndex = new HashMap<>();

        for (int i = 0; i < idList.size(); i++) {
            idToIndex.put(idList.get(i), i);
        }

        int n = idList.size();
        double[] balance = new double[n];

        for(Debt debt: debts) {
            int fromIdx = idToIndex.get(debt.getDebtor().getId());
            int toIdx = idToIndex.get(debt.getCreditor().getId());
            double amount = debt.getAmount();

            balance[fromIdx] -= amount;
            balance[toIdx] += amount;
        }

        List<Integer> idxNonZero = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (Math.abs(balance[i]) >= 1e-6) {
                idxNonZero.add(i);
            }
        }

        int m = idxNonZero.size();
        if (m == 0) {
            return new ArrayList<>();
        }

        double[] debt = new double[m];
        int[] idxToUserIdx = new int[m];

        for (int i = 0; i < m; i++) {
            int globalIdx = idxNonZero.get(i);
            debt[i] = balance[globalIdx];
            idxToUserIdx[i] = globalIdx;
        }

        BestResult best = new BestResult();
        best.minTransactions = Integer.MAX_VALUE;
        dfs(0, debt, idxToUserIdx, new ArrayList<>(), best, idList, group, userCache);

        return best.bestTransactions != null ? best.bestTransactions : new ArrayList<>();
    }

    private void greedyOptimize() {

    }
}