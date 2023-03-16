package com.escript.service;

import com.escript.FXApp;
import com.escript.data.IdPair;
import com.escript.data.MessageDbRepo;
import com.escript.domain.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.ArrayList;

public class MessageService {
    private final ObservableList<Message> messageList;
    private final MessageDbRepo messageRepo;
    private ScheduledService<Void> updaterHandle;

    public MessageService() {
        this.messageList = FXCollections.observableArrayList();
        this.messageRepo = new MessageDbRepo();
        GuiContext.getMessageFriendProperty().addListener(observable -> {
            messageList.clear();
            updateList();
        });
    }

    public ObservableList<Message> getMessageList() {
        return messageList;
    }

    private void updateList() {
        try {
            var idPair = new IdPair(
                    GuiContext.getAccount().getIdentifier(),
                    GuiContext.getMessageFriend().getIdentifier()
            );
            var conversation = new ArrayList<>(messageRepo.getConversation(idPair));
            if(conversation.isEmpty())
                return;

            if(messageList.isEmpty())
                messageList.addAll(conversation);

            var lastMessage = messageList.get(0);
            for(var message : messageList) {
                if(message.getDateSent().isAfter(lastMessage.getDateSent()))
                    lastMessage = message;
            }

            for(var message : conversation) {
                if(message.getDateSent().isAfter(lastMessage.getDateSent()))
                    messageList.add(message);
            }
        } catch (Exception e) {
            FXApp.exceptionHandler(e);
        }
    }

    public void sendMessage(Message message) {
        messageRepo.add(message);
    }

    public void startAutoUpdate() {
        updaterHandle = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        Platform.runLater(
                                () -> GuiContext.getMessageServiceProvider().updateList()
                        );
                        return null;
                    }
                };
            }
        };
        updaterHandle.setDelay(Duration.millis(500));
        updaterHandle.setPeriod(Duration.millis(3000));
        updaterHandle.start();
    }

    public void stopAutoUpdate() {
        updaterHandle.cancel();
    }

    public void closeConnection() throws SQLException {
        messageRepo.closeConnection();
    }
}
