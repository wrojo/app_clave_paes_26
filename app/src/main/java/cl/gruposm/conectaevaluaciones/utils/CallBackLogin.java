package cl.gruposm.conectaevaluaciones.utils;

import cl.gruposm.conectaevaluaciones.object.User;

public interface CallBackLogin {

    void onSucess(User user);
    void onErrorServer();
    void onErrorUnauthorized();
    void onErrorToken();
}
