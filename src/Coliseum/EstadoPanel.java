import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import modelo.EstadoServicio;
import servicio.SaludServicio;

/** R12: pantalla que verifica la conexión con la base de datos (equivale al /health de un proyecto web). */
class EstadoPanel extends JPanel {

    private final SaludServicio salud = new SaludServicio();
    private final JLabel estado = new JLabel("Sin verificar");
    private final JLabel detalle = new JLabel(" ");
    private final JButton verificarBtn = BotonEstilo.crear("Verificar conexión");

    EstadoPanel() {
        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 20, 8, 20);

        gbc.gridy = 0;
        add(UI.titulo("Estado del servicio"), gbc);

        estado.setFont(new Font("SansSerif", Font.BOLD, 18));
        estado.setForeground(Tema.TEXTO_SECUNDARIO);
        gbc.gridy = 1;
        add(estado, gbc);

        detalle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detalle.setForeground(Tema.TEXTO_SECUNDARIO);
        gbc.gridy = 2;
        add(detalle, gbc);

        verificarBtn.addActionListener(e -> verificar());
        gbc.gridy = 3;
        gbc.insets = new Insets(18, 20, 8, 20);
        add(verificarBtn, gbc);
    }

    /** Consulta la base en segundo plano para no congelar la ventana. */
    void verificar() {
        verificarBtn.setEnabled(false);
        estado.setForeground(Tema.TEXTO_SECUNDARIO);
        estado.setText("Verificando...");
        new SwingWorker<EstadoServicio, Void>() {
            @Override
            protected EstadoServicio doInBackground() {
                return salud.verificar();
            }

            @Override
            protected void done() {
                try {
                    EstadoServicio e = get();
                    estado.setForeground(e.isOk() ? new Color(90, 200, 120) : new Color(220, 90, 90));
                    estado.setText((e.isOk() ? "✔ " : "✖ ") + e.getMensaje());
                    detalle.setText(e.isOk()
                            ? "Destino: " + e.getDestino() + "  ·  " + e.getLatenciaMs() + " ms  ·  conexión cifrada (SSL): "
                            + (e.isSsl() ? "sí" : "no")
                            : "Destino: " + e.getDestino());
                } catch (Exception ex) {
                    estado.setText("No se pudo verificar.");
                }
                verificarBtn.setEnabled(true);
            }
        }.execute();
    }
}
