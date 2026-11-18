package modelo;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Rutina habitual de un día de la semana de un usuario. */
public class RutinaSemanal {
    private final int usuarioId;
    private final DayOfWeek dia;
    private final String resumen;
    private final String ejercicios;
    private final LocalTime hora; // puede ser null

    public RutinaSemanal(int usuarioId, DayOfWeek dia, String resumen, String ejercicios, LocalTime hora) {
        this.usuarioId = usuarioId;
        this.dia = dia;
        this.resumen = resumen;
        this.ejercicios = ejercicios;
        this.hora = hora;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public DayOfWeek getDia() {
        return dia;
    }

    public String getResumen() {
        return resumen;
    }

    public String getEjercicios() {
        return ejercicios;
    }

    public LocalTime getHora() {
        return hora;
    }
}
