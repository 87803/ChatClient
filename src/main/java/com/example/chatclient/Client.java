package com.example.chatclient;

import com.alibaba.fastjson.JSON;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client implements Runnable{
    private String userID;//当前登录用户信息
    protected String nickName;
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private boolean Running = true;
    private UserController userController;
    public Client() throws IOException {
        this.userID = null;
        this.nickName = null;
        String host = "127.0.0.1";
        int serverPort = 8888;
        socket = new Socket(host, serverPort);
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
    }

    public String register(String nickName, String pwd) throws IOException {    //返回注册的新账号，注册失败返回-1
        Map<String,String> sendMessage = new HashMap<>();
        sendMessage.put("method", "0"); //0表示注册
        sendMessage.put("nickName", nickName);
        pwd = DigestUtils.md5Hex(pwd);  //加密
        sendMessage.put("pwd", pwd);
        System.out.println(sendMessage.get("nickName"));

        String jsonString = JSON.toJSONString(sendMessage);
        output.writeUTF(jsonString);

        String serverInputStr = input.readUTF();//这里要注意和客户端输出流的写方法对应,否则会抛 EOFException
        // 处理客户端数据
        System.out.println("服务器发过来的内容:" + serverInputStr);
        Map<String,String> maps = JSON.parseObject(serverInputStr, Map.class);
        String newID = "-1";
        if(maps.get("method").equals("01"))
            newID = maps.get("id");

        closeConnection();
        return newID;
    }

    public String login(String userID, String pwd) throws IOException {
        Map<String,String> sendMessage = new HashMap<>();
        sendMessage.put("method", "1"); //1表示登录
        sendMessage.put("userid", userID);
        sendMessage.put("pwd", DigestUtils.md5Hex(pwd));
        //发送数据到客服务器
        String jsonString = JSON.toJSONString(sendMessage);
        output.writeUTF(jsonString);

        //接收服务器返回的登录结果
        String serverInputStr = input.readUTF();//这里要注意和客户端输出流的写方法对应,否则会抛 EOFException
        // 处理客户端数据
        System.out.println("服务器发过来的内容:" + serverInputStr);
        Map<String,String> maps = JSON.parseObject(serverInputStr, Map.class);
        String str = "fail";
        if(maps.get("method").equals("11"))//登录成功
        {
            this.userID = userID;
            this.nickName = maps.get("nickName");
            //new Thread(this).start();
            return "success";
        }
        else if(maps.get("method").equals("10"))
            str = "账户或密码错误";
        else if(maps.get("method").equals("12"))
            str = "用户已登录，请勿重复登录";

        closeConnection();
        return str;
    }

    public void sendMessage(String destID, String msg, String time)
    {
        Map<String,String> sendMessage = new HashMap<>();
        sendMessage.put("method", "5"); //5表示私聊
        sendMessage.put("sourceID", this.userID);
        sendMessage.put("destID", destID);
        sendMessage.put("msg", msg);
        sendMessage.put("time", nickName + " " + time);
        String jsonString = JSON.toJSONString(sendMessage);
        try {
            output.writeUTF(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void logout() throws IOException {
        Map<String,String> sendMessage = new HashMap<>();
        sendMessage.put("method", "2"); //1表示注销
        //发送数据到客户端
        String jsonString = JSON.toJSONString(sendMessage);
        Running = false;
        output.writeUTF(jsonString);

        closeConnection();
    }
    private void closeConnection() throws IOException {
        this.output.close();
        this.input.close();
        this.socket.close();
    }

    @Override
    public void run() {
        while (Running) {
            try {
                String serverInputStr = input.readUTF();
                Map<String, String> maps = JSON.parseObject(serverInputStr, Map.class);
                System.out.println("服务器发过来的内容:" + serverInputStr);

                switch (maps.get("method")) {
                    case "3" ->   //好友列表增加好友信息
                    {
                        //解决Not on FX application thread; currentThread = Thread-3
                        Platform.runLater(() -> {
                            userController.friendList.add(new FriendInfo(maps.get("userID"), maps.get("nickName")));//更新JavaFX的主线程的代码放在此处
                        });
                        userController.updateFriendListArea();
                    }
                    case "4" ->   //有好友下线，好友列表删除好友信息
                    {
                        //解决Not on FX application thread; currentThread = Thread-3
                        Platform.runLater(() -> {
                            for (int i = 0; i < userController.friendList.size(); i++)
                                if (userController.friendList.get(i).userID.equals(maps.get("userID")))
                                    userController.friendList.remove(i);//更新JavaFX的主线程的代码放在此处
                        });
                        userController.updateFriendListArea();
                    }
                    case "5" -> {
                        //解决Not on FX application thread; currentThread = Thread-3
                        Platform.runLater(() -> {
                            for (int i = 0; i < userController.friendList.size(); i++)
                                if ((maps.get("destID").equals("11111") && userController.friendList.get(i).userID.equals("11111")) || userController.friendList.get(i).userID.equals(maps.get("sourceID"))) {
                                    userController.friendList.get(i).chatRecord.add(maps.get("time"));
                                    userController.friendList.get(i).chatRecord.add(maps.get("msg"));
                                    userController.updateChatRecordArea();
                                    if (userController.selectUser == null || (!maps.get("sourceID").equals(userController.selectUser.userID) && !maps.get("destID").equals(userController.selectUser.userID))) {
                                        FriendInfo newInfo = userController.friendList.get(i);
                                        newInfo.userName = "· " + newInfo.userName;
                                        //System.out.println(newInfo);
                                        userController.friendList.set(i, newInfo);
                                        userController.updateFriendListArea();
                                    }
                                    break;
                                }

                        });
                        if (maps.get("sourceID").equals("10000") && maps.get("info")!=null &&maps.get("info").equals("forceLogout") && maps.get("targetID").equals(this.userID)) {
                            //收到来自服务器的下线消息

                            //解决Not on FX application thread; currentThread = Thread-3
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("错误");
                                alert.setHeaderText(null);
                                alert.setContentText("抱歉，服务器将您强制下线，请遵循聊天室规范");
                                alert.showAndWait();
                                Stage stage2 = (Stage) userController.helloLabel.getScene().getWindow();
                                stage2.close();
                                stage2.hide();
                                userController.logout();
                                Running = false;
                                    }
                            );

                        }
                    }
                }
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        }
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }
}
