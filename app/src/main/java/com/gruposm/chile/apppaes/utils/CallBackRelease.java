package com.gruposm.chile.apppaes.utils;

import com.gruposm.chile.apppaes.object.User;

public interface CallBackRelease {

    void onSucess(boolean success);
    void onErrorServer();
    void onErrorUnauthorized();
}
