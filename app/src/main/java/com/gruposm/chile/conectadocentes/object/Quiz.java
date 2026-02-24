package com.gruposm.chile.conectadocentes.object;

import java.util.List;

public class Quiz {
    private String id;
    private String cursoId;
    private String nombre;
    private int totalPreguntas;
    private int totalOpciones;
    private String tipo;
    private String correctas;
    private String fechaInicio;
    private String fechaFin;
    private int liberado;
    private String urlHojaRespuestas;
    private List<Result> results;
    private List<Student> students;
    private String origen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTotalPreguntas() {
        return totalPreguntas;
    }

    public void setTotalPreguntas(int totalPreguntas) {
        this.totalPreguntas = totalPreguntas;
    }

    public int getTotalOpciones() {
        return totalOpciones;
    }

    public void setTotalOpciones(int totalOpciones) {
        this.totalOpciones = totalOpciones;
    }

    public String getCorrectas() {
        return correctas;
    }

    public void setCorrectas(String correctas) {
        this.correctas = correctas;
    }

    public String getCursoId() {
        return cursoId;
    }

    public void setCursoId(String cursoId) {
        this.cursoId = cursoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getLiberado() {
        return liberado;
    }

    public void setLiberado(int liberado) {
        this.liberado = liberado;
    }

    public String getUrlHojaRespuestas() {
        return urlHojaRespuestas;
    }

    public void setUrlHojaRespuestas(String urlHojaRespuestas) {
        this.urlHojaRespuestas = urlHojaRespuestas;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
}
