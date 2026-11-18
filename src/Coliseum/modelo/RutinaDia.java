package modelo;

import java.time.LocalDate;

/** Nota puntual de una fecha del calendario. */
public class RutinaDia {
    private final int id;
    private final int usuarioId;
    private final String nombreUsuario; // solo lo completan las consultas con JOIN
    private final LocalDate fecha;
    private final String descripcion;

    public RutinaDia(int id, int usuarioId, String nombreUsuario, LocalDate fecha, String descripcion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
