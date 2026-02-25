package cl.gruposm.conectaevaluaciones.object;

public class Result {
    private String id;
    private String ensayo_id;
    private String rut;
    private int buenas;
    private int malas;
    private int omitidas;
    private int porcentaje;
    private String porcentajeStr;
    private String imagen;
    private String respuesta;
    private String estudianteId;
    private String fecha;
    private String curso_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnsayo_id() {
        return ensayo_id;
    }

    public void setEnsayo_id(String ensayo_id) {
        this.ensayo_id = ensayo_id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public int getBuenas() {
        return buenas;
    }

    public void setBuenas(int buenas) {
        this.buenas = buenas;
    }

    public int getMalas() {
        return malas;
    }

    public void setMalas(int malas) {
        this.malas = malas;
    }

    public int getOmitidas() {
        return omitidas;
    }

    public void setOmitidas(int omitidas) {
        this.omitidas = omitidas;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(int porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getPorcentajeStr() {
        return porcentajeStr;
    }

    public void setPorcentajeStr(String porcentajeStr) {
        this.porcentajeStr = porcentajeStr;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getCurso_id() {
        return curso_id;
    }

    public void setCurso_id(String curso_id) {
        this.curso_id = curso_id;
    }

}

