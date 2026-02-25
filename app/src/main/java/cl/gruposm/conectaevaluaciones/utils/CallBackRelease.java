package cl.gruposm.conectaevaluaciones.utils;

public interface CallBackRelease {

    void onSucess(boolean success);
    void onErrorServer();
    void onErrorUnauthorized();
}
