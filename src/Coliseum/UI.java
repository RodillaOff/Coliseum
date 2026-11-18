import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Utilidades de interfaz compartidas por los paneles nuevos (mismo estilo oscuro que el resto de la app). */
class UI {

    static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 26));
        l.setForeground(Tema.ACCENT);
        return l;
    }

    static JLabel texto(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(Tema.TEXTO);
        return l;
    }

    static JTextField campo(int columnas) {
        JTextField c = new JTextField(columnas);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(Tema.FONDO_SECUNDARIO);
        c.setForeground(Tema.TEXTO);
        c.setCaretColor(Tema.TEXTO);
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE_DIAS),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return c;
    }

    static DefaultTableModel modeloSoloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    static JScrollPane tabla(JTable t) {
        t.setBackground(Tema.FONDO_SECUNDARIO);
        t.setForeground(Tema.TEXTO);
        t.setGridColor(Tema.BORDE_DIAS);
        t.setRowHeight(24);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setSelectionBackground(Tema.ACCENT);
        t.setSelectionForeground(Color.BLACK);
        t.getTableHeader().setBackground(Tema.FONDO_DIAS);
        t.getTableHeader().setForeground(Tema.TEXTO);
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(Tema.FONDO_SECUNDARIO);
        sp.setBorder(BorderFactory.createLineBorder(Tema.BORDE_DIAS));
        sp.setPreferredSize(new Dimension(400, 180));
        return sp;
    }

    /** Muestra el mensaje (ya apto para el usuario) de una excepción del dominio. */
    static void error(Component padre, Exception e) {
        JOptionPane.showMessageDialog(padre, e.getMessage(), "Coliseum", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Coliseum", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Pide una contraseña con un campo oculto. Devuelve null si se cancela. */
    static String pedirContrasena(Component padre, String mensaje) {
        JPasswordField campo = new JPasswordField(18);
        Object[] contenido = {mensaje, campo};
        int r = JOptionPane.showConfirmDialog(padre, contenido, "Coliseum", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return r == JOptionPane.OK_OPTION ? new String(campo.getPassword()) : null;
    }
}
