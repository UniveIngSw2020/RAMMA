package com.example.rent_scio1.utils;

import android.app.Application;

// classe di utilità contente informazioni dell'utente registrato

public class UserClient extends Application {
    private static User user = null;
    private static Run run = null;
    private static User trader = null;
    public static User getUser() { return user; }
    public static void setUser (User user1) { user = user1; }

    public static Run getRun() {
        return run;
    }

    public static void setRun(Run run1) {
        run = run1;
    }

    public  static User getTrader() {
        return trader;
    }

    public static void setTrader(User user){
        trader = user;
    }
}
