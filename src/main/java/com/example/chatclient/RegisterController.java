package com.example.chatclient;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.*;

public class RegisterController {
    public Button registerButton;
    public TextField inputNickName;
    public PasswordField inputPwd;
    public void clickRegisterButton(){
        String pwd = inputPwd.getText();
        String nickName = inputNickName.getText();
        if (nickName.length() == 0 || nickName.length() >= 20) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("昵称长度错误");
            alert.showAndWait();
            return;
        }
        if (pwd.length() <= 6 || pwd.length() >= 18) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("密码长度错误，应为6-18位");
            alert.showAndWait();
            return;
        }

        try {
            String newID = new Client().register(nickName, pwd);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            if(!newID.equals("-1"))
            {
                alert.setTitle("注册成功");
                alert.setContentText("注册成功，您的账号是：" + newID);
            }
            else
            {
                alert.setTitle("注册失败");
                alert.setHeaderText(null);
                alert.setContentText("注册出现错误");
            }
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("注册失败");
            alert.setHeaderText(null);
            alert.setContentText("无法连接到服务器");
            alert.showAndWait();
            //throw new RuntimeException(e);
        }
    }
}
