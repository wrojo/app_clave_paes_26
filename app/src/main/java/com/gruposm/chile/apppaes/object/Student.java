package com.gruposm.chile.apppaes.object;

public class Student {
    private String id;
    private String apellidos;
    private String nombres;
    private String cursoId;
    private int rutInt;
    private String rut;
    public Student()
    {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRut(String rut)
    {

        this.rut = rut;
    }
    public String getRut() {
        if(this.rut.charAt(0) == '0')
        {

            StringBuilder sb = new StringBuilder(this.rut);
            sb.deleteCharAt(0);
            return sb.toString();
        }
        return this.rut;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getCursoId() {
        return cursoId;
    }

    public void setCursoId(String cursoId) {
        this.cursoId = cursoId;
    }

    public int getRutInt() {
        return rutInt;
    }

    public void setRutInt(int rutInt) {
        this.rutInt = rutInt;
    }
    public String getNombreCompleto()
    {
        return this.apellidos+" "+this.nombres;
    }
    public String getLetra()
    {
        return getNombreCompleto().substring(0, 1);
    }
}

