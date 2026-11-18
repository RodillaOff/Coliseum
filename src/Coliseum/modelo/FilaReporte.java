package modelo;

/** Una fila del reporte por usuario (resultado de JOIN + COUNT + GROUP BY). */
public class FilaReporte {
    private final String usuario;
    private final Rol rol;
    private final long notas;
    private final long diasConfigurados;

    public FilaReporte(String usuario, Rol rol, long notas, long diasConfigurados) {
        this.usuario = usuario;
        this.rol = rol;
        this.notas = notas;
        this.diasConfigurados = diasConfigurados;
    }

    public String getUsuario() {
        return usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public long getNotas() {
        return notas;
    }

    public long getDiasConfigurados() {
        return diasConfigurados;
    }
}
