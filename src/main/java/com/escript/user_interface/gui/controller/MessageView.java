package com.escript.user_interface.gui.controller;

import com.escript.FXApp;
import com.escript.data.ArrowIdPair;
import com.escript.domain.Message;
import com.escript.service.GuiContext;
import com.escript.service.GuiService;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class MessageView {

    public Text topBarFriendName;
    public VBox messageBubbleZone;
    public TextField messageBox;

    private ObservableList<Message> messageList;

    public void initialize() throws IOException {
        topBarFriendName.setText(GuiContext.getMessageFriend().getUser().getUsername());
        messageList = GuiContext.getMessageServiceProvider().getMessageList();
        messageList.addListener((ListChangeListener<Message>) c -> {
            try {
                c.next();
                updateMessages(c.getAddedSubList());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        drawAllMessages();
        GuiContext.getMessageServiceProvider().startAutoUpdate();
    }

    public void updateMessages(List<? extends Message> addedSubList) throws IOException {
        for(Message m : addedSubList) {
            drawMessage(m);
        }
    }

    public void drawAllMessages() throws IOException {
        for(Message m : messageList) {
            drawMessage(m);
        }
    }

    public void drawMessage(Message message) throws IOException {
        String fxmlPath, sender;
        if(message.getIdPair().getSenderId().equals(GuiContext.getAccount().getIdentifier())) {
            fxmlPath = "/fxml/RightMessageBubble.fxml";
            sender = GuiContext.getAccount().getUser().getUsername();
        } else {
            fxmlPath = "/fxml/LeftMessageBubble.fxml";
            sender = GuiContext.getMessageFriend().getUser().getUsername();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(
                FXApp.class.getResource(fxmlPath)
        );

        Node messageNode = fxmlLoader.load();
        Text headText = (Text) messageNode.lookup("#messageHead");
        headText.setText(sender);

        Text bodyText = (Text) messageNode.lookup("#messageBody");
        bodyText.setText(message.getText());

        messageBubbleZone.getChildren().add(messageNode);
    }

    public void sendMessage() {
        String text = messageBox.getText();
        if(text.isBlank()) {
            messageBox.clear();
            return;
        }

        var message = new Message(text, new ArrowIdPair(
                GuiContext.getAccount().getIdentifier(),
                GuiContext.getMessageFriend().getIdentifier()
                ),
                LocalDateTime.now()
        );

        GuiContext.getMessageServiceProvider().sendMessage(message);
        messageBox.clear();
    }

    public void loadMainPage(ActionEvent actionEvent) throws IOException {
        GuiContext.getMessageServiceProvider().stopAutoUpdate();
        GuiService.setView(GuiService.views.MAIN);
    }

    public void sendIfEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
            sendMessage();
    }
}
