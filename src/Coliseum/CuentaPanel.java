import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import excepciones.ColiseumException;
import modelo.Usuario;
import servicio.UsuarioServicio;

/** Mi cuenta: consulta de datos, modificación (nombre y contraseña) y baja de la propia cuenta (CRUD de usuarios). */
class CuentaPanel extends JPanel {

    private final UsuarioServicio servicio = new UsuarioServicio();
    private final String nombreActual;
    private final JLabel datos = new JLabel(" ");

    CuentaPanel(String usuarioActual) {
        this.nombreActual = usuarioActual;
        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy = 0;
        add(UI.titulo("Mi cuenta"), gbc);

        datos.setFont(new Font("SansSerif", Font.PLAIN, 14));
        datos.setForeground(Tema.TEXTO);
        gbc.gridy = 1;
        add(datos, gbc);

        JButton contrasenaBtn = BotonEstilo.crear("Cambiar contraseña");
        JButton nombreBtn = BotonEstilo.crear("Cambiar nombre de usuario", Tema.FONDO_DIAS, Tema.TEXTO);
        JButton eliminarBtn = BotonEstilo.crear("Eliminar mi cuenta", new java.awt.Color(170, 60, 60), Tema.TEXTO);
        contrasenaBtn.addActionListener(e -> cambiarContrasena());
        nombreBtn.addActionListener(e -> cambiarNombre());
        eliminarBtn.addActionListener(e -> eliminarCuenta());
        gbc.gridy = 2;
        gbc.insets = new Insets(16, 20, 6, 20);
        add(contrasenaBtn, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(6, 20, 6, 20);
        add(nombreBtn, gbc);
        gbc.gridy = 4;
        add(eliminarBtn, gbc);
        refrescar();
    }

    void refrescar() {
        try {
            Optional<Usuario> u = servicio.buscarPorNombre(nombreActual);
            datos.setText(u.isPresent()
                    ? "<html>Usuario: <b>" + u.get().getNombre() + "</b> &nbsp;·&nbsp; Rol: <b>"
                    + u.get().getRol().getEtiqueta() + "</b></html>"
                    : "No se encontró la cuenta.");
        } catch (ColiseumException e) {
            datos.setText(e.getMessage());
        }
    }

    private Usuario usuario() throws ColiseumException {
        return servicio.buscarPorNombre(nombreActual).orElseThrow(() -> new ColiseumException("No se encontró la cuenta."));
    }

    private void cambiarContrasena() {
        String actual = UI.pedirContrasena(this, "Contraseña actual:");
        if (actual == null) {
            return;
        }
        String nueva = UI.pedirContrasena(this, "Contraseña nueva (mínimo 6 caracteres):");
        if (nueva == null) {
            return;
        }
        try {
            servicio.cambiarContrasena(usuario().getId(), actual, nueva);
            UI.info(this, "Contraseña actualizada.");
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }

    private void cambiarNombre() {
        String nuevo = JOptionPane.showInputDialog(this, "Nuevo nombre de usuario:", "Coliseum", JOptionPane.PLAIN_MESSAGE);
        if (nuevo == null) {
            return;
        }
        String clave = UI.pedirContrasena(this, "Confirmá con tu contraseña:");
        if (clave == null) {
            return;
        }
        try {
            servicio.renombrar(usuario().getId(), clave, nuevo);
            UI.info(this, "Nombre actualizado. La aplicación se cierra: volvé a iniciar sesión con el nombre nuevo.");
            System.exit(0);
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }

    private void eliminarCuenta() {
        int r = JOptionPane.showConfirmDialog(this,
                "Se borrará tu cuenta y todas tus rutinas. Esta acción no se puede deshacer. ¿Continuar?",
                "Coliseum", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        String clave = UI.pedirContrasena(this, "Confirmá con tu contraseña:");
        if (clave == null) {
            return;
        }
        try {
            servicio.eliminarCuenta(usuario().getId(), clave);
            UI.info(this, "Cuenta eliminada. La aplicación se cierra.");
            System.exit(0);
        } catch (ColiseumException e) {
            UI.error(this, e);
        }
    }
}
