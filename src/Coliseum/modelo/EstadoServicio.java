package modelo;

/** Resultado de la verificación de salud (R12). */
public class EstadoServicio {
    private final boolean ok;
    private final String mensaje;
    private final long latenciaMs;
    private final String destino;
    private final boolean ssl;

    public EstadoServicio(boolean ok, String mensaje, long latenciaMs, String destino, boolean ssl) {
        this.ok = ok;
        this.mensaje = mensaje;
        this.latenciaMs = latenciaMs;
        this.destino = destino;
        this.ssl = ssl;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMensaje() {
        return mensaje;
    }

    public long getLatenciaMs() {
        return latenciaMs;
    }

    public String getDestino() {
        return destino;
    }

    public boolean isSsl() {
        return ssl;
    }
}
