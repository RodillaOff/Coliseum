import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import excepciones.ColiseumException;
import modelo.Rol;
import modelo.Usuario;
import servicio.UsuarioServicio;

/** Administración de usuarios. Solo se muestra a quien tiene rol ADMIN (y el servicio lo vuelve a comprobar). */
class AdminPanel extends JPanel {

    private final UsuarioServicio servicio = new UsuarioServicio();
    private final Usuario actor;
    private final DefaultTableModel modelo = UI.modeloSoloLectura("Id", "Usuario", "Rol", "Notificaciones");
    private final JTable tabla = new JTable(modelo);

    AdminPanel(Usuario actor) {
        this.actor = actor;
        setLayout(new BorderLayout(10, 10));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        add(UI.titulo("Administración de usuarios"), BorderLayout.NORTH);
        add(UI.tabla(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.setBackground(Tema.FONDO);
        JButton actualizar = BotonEstilo.crear("Actualizar", Tema.FONDO_DIAS, Tema.TEXTO);
        JButton rolBtn = BotonEstilo.crear("Cambiar rol");
        JButton claveBtn = BotonEstilo.crear("Restablecer contraseña", Tema.FONDO_DIAS, Tema.TEXTO);
        JButton eliminarBtn = BotonEstilo.crear("Eliminar", new java.awt.Color(170, 60, 60), Tema.TEXTO);
        actualizar.addActionListener(e -> cargar());
        rolBtn.addActionListener(e -> cambiarRol());
        claveBtn.addActionListener(e -> restablecer());
        eliminarBtn.addActionListener(e -> eliminar());
        botones.add(actualizar);
        botones.add(rolBtn);
        botones.add(claveBtn);
        botones.add(eliminarBtn);
        add(botones, BorderLayout.SOUTH);
    }

    void cargar() {
        try {
            List<Usuario> usuarios = servicio.listar(actor);
            modelo.setRowCount(0);
            for (Usuario u : usuarios) {
                Boolean n = u.getNotificaciones();
                modelo.addRow(new Object[]{u.getId(), u.getNombre(), u.getRol().getEtiqueta(),
                        n == null ? "sin elegir" : (n ? "activadas" : "desactivadas")});
            }
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }

    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UI.info(this, "Elegí un usuario de la tabla.");
            return -1;
        }
        return (Integer) modelo.getValueAt(fila, 0);
    }

    private void cambiarRol() {
        int id = idSeleccionado();
        if (id < 0) {
            return;
        }
        Object[] opciones = {Rol.USUARIO.getEtiqueta(), Rol.ADMIN.getEtiqueta()};
        int r = JOptionPane.showOptionDialog(this, "Nuevo rol del usuario:", "Coliseum", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (r < 0) {
            return;
        }
        try {
            servicio.cambiarRol(actor, id, r == 1 ? Rol.ADMIN : Rol.USUARIO);
            cargar();
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }

    private void restablecer() {
        int id = idSeleccionado();
        if (id < 0) {
            return;
        }
        String nueva = UI.pedirContrasena(this, "Contraseña nueva para ese usuario (mínimo 6 caracteres):");
        if (nueva == null) {
            return;
        }
        try {
            servicio.restablecerContrasena(actor, id, nueva);
            UI.info(this, "Contraseña restablecida.");
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }

    private void eliminar() {
        int id = idSeleccionado();
        if (id < 0) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Se borrará el usuario y todas sus rutinas. ¿Continuar?",
                "Coliseum", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminarUsuario(actor, id);
            cargar();
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }
}
