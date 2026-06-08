package project.cheqmate.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.cheqmate.event.ChequeAddedEvent;
import project.cheqmate.event.NotificationMessage;
import project.cheqmate.event.UserAddedToGroupEvent;
import project.cheqmate.service.NotificationDispatcher;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationDispatcher notificationDispatcher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserAddedToGroup(UserAddedToGroupEvent event) {
        String title = "Новая группа";
        String body = String.format("%s добавил Вас в группу \"%s\"",
                event.inviterName(), event.groupName());

        notificationDispatcher.dispatch(new NotificationMessage(
                event.targetUsername(), "GROUP_INVITE", title, body, null));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChequeAdded(ChequeAddedEvent event) {
        String title = "Новый чек";
        String body = String.format("%s добавил чек \"%s\" в группу \"%s\"",
                event.creatorName(), event.chequeName(), event.groupName());

        for (String username : event.targetUsernames()) {
            if (!username.equals(event.creatorName())) {
                notificationDispatcher.dispatch(new NotificationMessage(
                        username, "CHEQUE_ADDED", title, body, null));
            }
        }
    }
}
