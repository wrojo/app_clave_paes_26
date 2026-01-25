package com.gruposm.chile.conectadocentes.utils;

import com.gruposm.chile.conectadocentes.object.User;

public interface CallBackLogin {

    void onSucess(User user);
    void onErrorServer();
    void onErrorUnauthorized();
    void onErrorToken();
}
