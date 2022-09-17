package com.example.chatclient;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class UserController{
    @FXML
    public ListView friendGroup;
    public Label helloLabel;
    public Label userNameLabel;
    public Button sendButton;
    public TextArea inputMsg;
    public ListView chatRecordListView;
    private Client client;
    public FriendInfo selectUser;
    ObservableList<FriendInfo> friendList;

    public UserController(){
        //System.out.println("hello");
        friendList = FXCollections.observableArrayList(new FriendInfo("11111","公共聊天室"));
        selectUser = null;
    }

    @FXML
    public void initialize()
    {
        //添加列表监听
       //friendGroup.getSelectionModel().selectedItemProperty().addListener(new NoticeListItemChangeListener());
        friendGroup.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> {
            //这里写自己的代码
            //System.out.println(newValue);
            selectUser = ((FriendInfo) newValue);
            updateChatRecordArea();
            System.out.println("当前选择的聊天对象: " + selectUser.userID);
            for (FriendInfo friendInfo : friendList) {
                if (friendInfo.userID.equals(selectUser.userID)) {
                    if (friendInfo.userName.charAt(0) == '·')
                        friendInfo.userName = friendInfo.userName.substring(2);
                    /* updateFriendListArea();*/
                    break;
                }
            }
        });
    }




    public void setClient(Client client)
    {
        this.client = client;
    }

    public void updateUserInfo() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour>7 && hour<=12)
            this.helloLabel.setText("上午好");
        else if(hour>12 && hour<=20)
            this.helloLabel.setText("下午好");
        else
            this.helloLabel.setText("晚上好");
        this.userNameLabel.setText(client.nickName);
    }

    public void logout() {
        try {
            client.logout();
            System.exit(0);
        } catch (IOException e) {
            System.exit(0);
            //throw new RuntimeException(e);
        }
    }

    public void clickSendButton() {
        if(selectUser == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("请选择一个聊天对象");
            alert.showAndWait();
        }
        else
        {
            String msg = inputMsg.getText();
            inputMsg.clear();
            java.util.Date day=new Date();
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = sdf.format(day);

            client.sendMessage(selectUser.userID, msg, time);

            selectUser.chatRecord.add(client.nickName + " " +time);
            selectUser.chatRecord.add(msg);
            updateChatRecordArea();
        }
    }

    public void updateChatRecordArea()
    {
        if(selectUser != null)
            chatRecordListView.setItems(selectUser.chatRecord);
    }

    public void updateFriendListArea()
    {
        friendGroup.setItems(friendList);
    }
}
