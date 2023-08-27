package com.gruposm.chile.apppaes.utils;

import com.gruposm.chile.apppaes.object.User;

public interface CallBackLogin {

    void onSucess(User user);
    void onErrorServer();
    void onErrorUnauthorized();
    void onErrorToken();
}
