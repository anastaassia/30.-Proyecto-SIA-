package com.mycompany.mavenproject1.modelo;

/**
 * Estudiante de la universidad que puede postular a convenios de intercambio.
 * Se identifica por su RUT, que es la clave del mapa de estudiantes en {@code Control}.
 */
public class Estudiante {

    private String rut;
    private String nombre;
    private String carrera;
    private int anioIngreso;
    private EstadoProceso estadoProceso;

    /**
     * Crea un estudiante en estado {@link EstadoProceso#POSTULACION}.
     *
     * @param rut         RUT en formato 12.345.678-9
     * @param nombre      nombre completo
     * @param carrera     carrera que cursa
     * @param anioIngreso año de ingreso a la universidad
     */
    public Estudiante(String rut, String nombre, String carrera, int anioIngreso) {
        this.rut = rut;
        this.nombre = nombre;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
        this.estadoProceso = EstadoProceso.POSTULACION;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public int getAnioIngreso() {
        return anioIngreso;
    }

    public void setAnioIngreso(int anioIngreso) {
        this.anioIngreso = anioIngreso;
    }

    public EstadoProceso getEstadoProceso() {
        return estadoProceso;
    }

    public void setEstadoProceso(EstadoProceso estadoProceso) {
        this.estadoProceso = estadoProceso;
    }

    /** Dos estudiantes son el mismo si tienen el mismo RUT. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Estudiante)) {
            return false;
        }
        return rut != null && rut.equals(((Estudiante) o).rut);
    }

    @Override
    public int hashCode() {
        return rut == null ? 0 : rut.hashCode();
    }

    @Override
    public String toString() {
        return rut + " - " + nombre;
    }
}
