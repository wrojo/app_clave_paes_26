package cl.gruposm.conectaevaluaciones.utils;

public interface CallBackScore {

    void onSucess(boolean success);
    void onErrorServer();
    void onErrorUnauthorized();
    void onErrorNoData();
}
