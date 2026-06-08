package project.cheqmate.service;

import project.cheqmate.event.NotificationMessage;

public interface NotificationDispatcher {
    void dispatch(NotificationMessage message);
}
