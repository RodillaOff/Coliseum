import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import excepciones.ColiseumException;
import excepciones.ValidacionException;
import modelo.FilaReporte;
import modelo.RutinaDia;
import modelo.Usuario;
import servicio.ReporteServicio;

/** R6: búsqueda con filtros (con JOIN) y reporte con COUNT/AVG y GROUP BY. El administrador ve a todos. */
class ReportesPanel extends JPanel {

    private final ReporteServicio servicio = new ReporteServicio();
    private final Usuario usuario;

    private final JTextField textoCampo = UI.campo(14);
    private final JTextField desdeCampo = UI.campo(8);
    private final JTextField hastaCampo = UI.campo(8);
    private final DefaultTableModel resultados = UI.modeloSoloLectura("Usuario", "Fecha", "Nota");
    private final DefaultTableModel resumen = UI.modeloSoloLectura("Usuario", "Rol", "Notas", "Días configurados");
    private final DefaultTableModel meses = UI.modeloSoloLectura("Mes", "Notas");
    private final JLabel promedio = UI.texto(" ");
    private final JLabel mensaje = UI.texto(" ");

    ReportesPanel(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(10, 10));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        add(UI.titulo("Reportes"), BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Buscar notas", construirBusqueda());
        pestanas.addTab("Resumen", construirResumen());
        add(pestanas, BorderLayout.CENTER);
    }

    private JPanel construirBusqueda() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Tema.FONDO);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBackground(Tema.FONDO);
        filtros.add(UI.texto("Texto:"));
        filtros.add(textoCampo);
        filtros.add(UI.texto("Desde (AAAA-MM-DD):"));
        filtros.add(desdeCampo);
        filtros.add(UI.texto("Hasta:"));
        filtros.add(hastaCampo);
        JButton buscar = BotonEstilo.crear("Buscar");
        buscar.addActionListener(e -> buscar());
        filtros.add(buscar);
        p.add(filtros, BorderLayout.NORTH);
        p.add(UI.tabla(new JTable(resultados)), BorderLayout.CENTER);
        p.add(mensaje, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construirResumen() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Tema.FONDO);
        JPanel centro = new JPanel(new java.awt.GridLayout(2, 1, 8, 8));
        centro.setBackground(Tema.FONDO);
        centro.add(UI.tabla(new JTable(resumen)));
        centro.add(UI.tabla(new JTable(meses)));
        p.add(centro, BorderLayout.CENTER);
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        abajo.setBackground(Tema.FONDO);
        JButton actualizar = BotonEstilo.crear("Actualizar");
        actualizar.addActionListener(e -> cargarResumen());
        abajo.add(actualizar);
        abajo.add(promedio);
        p.add(abajo, BorderLayout.SOUTH);
        return p;
    }

    /** Se llama al abrir la pestaña para mostrar datos frescos. */
    void cargar() {
        buscar();
        cargarResumen();
    }

    private LocalDate leerFecha(String texto, String nombre) throws ValidacionException {
        String t = texto.trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(t);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("La fecha \"" + nombre + "\" debe tener el formato AAAA-MM-DD.");
        }
    }

    private void buscar() {
        try {
            List<RutinaDia> filas = servicio.buscar(usuario, textoCampo.getText(),
                    leerFecha(desdeCampo.getText(), "desde"), leerFecha(hastaCampo.getText(), "hasta"));
            resultados.setRowCount(0);
            for (RutinaDia r : filas) {
                resultados.addRow(new Object[]{r.getNombreUsuario(), r.getFecha(), r.getDescripcion()});
            }
            mensaje.setText(filas.size() + " nota(s) encontrada(s)" + (filas.size() == 200 ? " (se muestran las 200 más recientes)" : "."));
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }

    private void cargarResumen() {
        try {
            resumen.setRowCount(0);
            for (FilaReporte f : servicio.resumenPorUsuario(usuario)) {
                resumen.addRow(new Object[]{f.getUsuario(), f.getRol().getEtiqueta(), f.getNotas(), f.getDiasConfigurados()});
            }
            meses.setRowCount(0);
            for (Map.Entry<String, Long> m : servicio.notasPorMes(usuario).entrySet()) {
                meses.addRow(new Object[]{m.getKey(), m.getValue()});
            }
            promedio.setText(usuario.puedeAdministrar()
                    ? String.format("Promedio de notas por usuario: %.2f", servicio.promedioNotasPorUsuario())
                    : " ");
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }
}
