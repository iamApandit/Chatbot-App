package com.example.sawaalkaro;

public class ChatsModal {
    public static String BOT_KEY = "bot";
    public static String USER_KEY = "user";

    private String message;
    private String sender;
    private long timeStamp;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp){
        this.timeStamp = timeStamp;
    }

    public ChatsModal(String message, String sender, long timeStamp) {
        this.message = message;
        this.sender = sender;
        this.timeStamp = timeStamp;

    }
}
