package com.example.rent_scio1.utils;

import android.app.Application;

public class UserClient extends Application {
    private User user = null;
    private User getUser() { return user; }
    public void setUser (User user) { this.user = user; }
}
