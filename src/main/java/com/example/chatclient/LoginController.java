package com.example.chatclient;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    public Button loginButton;
    public Button registerButton;
    public TextField idLabel;
    public PasswordField pwdLabel;

    public void clickLoginButton() {
        try {
            Client client = new Client();
            String loginResult = client.login(idLabel.getText(), pwdLabel.getText());
            if (loginResult.equals("success")) {
                Stage stage2 = (Stage) loginButton.getScene().getWindow();
                stage2.close();
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("user-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 900, 600);
                UserController userController = fxmlLoader.getController();
                Stage stage = new Stage();
                stage.setTitle("聊天室客户端");
                stage.setOnCloseRequest(windowEvent -> {
                    System.out.print("监听到窗口关闭");
                    userController.logout();
                });
                stage.setScene(scene);
                stage.show();

                client.setUserController(userController);
                userController.setClient(client);
                new Thread(client).start();
                userController.updateUserInfo();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("登录失败");
                alert.setHeaderText(null);
                alert.setContentText(loginResult);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("登录失败");
            alert.setHeaderText(null);
            alert.setContentText("无法连接到服务器");
            alert.showAndWait();
            //throw new RuntimeException(e);
        }
    }

    public void clickRegisterButton() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("register-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        Stage stage = new Stage();
        stage.setTitle("账号注册");
        stage.setScene(scene);
        stage.show();
    }
}