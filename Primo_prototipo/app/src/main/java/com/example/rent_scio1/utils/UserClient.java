package com.example.rent_scio1.utils;

import android.app.Application;

public class UserClient extends Application {
    private static User user = null;
    public static User getUser() { return user; }
    public static void setUser (User user1) { user = user1; }
}
