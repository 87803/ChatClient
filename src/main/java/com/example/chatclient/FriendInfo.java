package com.example.chatclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FriendInfo {
    protected String userID;
    protected String userName;
    protected ObservableList<String> chatRecord;  //存聊天记录
    FriendInfo(String userID, String userName){
        this.userID = userID;
        this.userName = userName;
        chatRecord = FXCollections.observableArrayList();
    }
    public String toString() {  //重写方法，便于在listview中显示
        return userName + "(" + userID + ")";
    }
}
