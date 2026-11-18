package consola;

import com.sun.net.httpserver.HttpServer;
import config.Configuracion;
import excepciones.ColiseumException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.EnumMap;
import java.util.Locale;
import modelo.RutinaSemanal;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import modelo.EstadoServicio;
import modelo.FilaReporte;
import modelo.Usuario;
import servicio.ReporteServicio;
import servicio.RutinaServicio;
import servicio.SaludServicio;
import servicio.UsuarioServicio;

/**
 * Modo SIN ventana de Coliseum, pensado para Docker y Kubernetes (la ventana Swing no corre en un contenedor).
 *
 *   --health    verifica la conexión y termina con código 0 (todo bien) o 1 (falla).
 *   --servicio  queda en ejecución y responde GET /health en el puerto APP_PORT (8080 por defecto).
 *   --consola   (por defecto) menú de texto: inicio de sesión, alta y consulta de notas, reporte y estado.
 */
public class ConsolaMain {

    private static final SaludServicio SALUD = new SaludServicio();
    private static final UsuarioServicio USUARIOS = new UsuarioServicio();
    private static final RutinaServicio RUTINAS = new RutinaServicio();
    private static final ReporteServicio REPORTES = new ReporteServicio();

    public static void main(String[] args) throws Exception {
        String modo = args.length == 0 ? "--consola" : args[0];
        switch (modo) {
            case "--health":
                EstadoServicio e = SALUD.verificar();
                System.out.println((e.isOk() ? "OK: " : "ERROR: ") + e.getMensaje());
                System.exit(e.isOk() ? 0 : 1);
                break;
            case "--servicio":
                iniciarServicio();
                break;
            case "--consola":
                consola();
                break;
            default:
                System.err.println("Modo desconocido: " + modo + ". Usá --consola, --servicio o --health.");
                System.exit(2);
        }
    }

    // ------------------------------------------------------------------ servicio HTTP /health

    private static void iniciarServicio() throws IOException, InterruptedException {
        int puerto = Integer.parseInt(Configuracion.obtener("APP_PORT", "8080"));
        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/health", intercambio -> {
            EstadoServicio estado = SALUD.verificar();
            byte[] cuerpo = (estado.isOk() ? "OK" : "ERROR").getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(estado.isOk() ? 200 : 503, cuerpo.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        servidor.start();
        System.out.println("Coliseum en modo servicio. GET http://0.0.0.0:" + puerto + "/health");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> servidor.stop(0)));
        Thread.currentThread().join(); // queda en ejecución hasta que lo detengan
    }

    // ------------------------------------------------------------------ consola interactiva

    private static void consola() {
        Scanner in = new Scanner(System.in, "UTF-8");
        System.out.println("=== Coliseum (consola) ===");
        Usuario usuario = null;
        while (usuario == null) {
            System.out.print("1) Iniciar sesión  2) Registrarse  0) Salir > ");
            String op = in.nextLine().trim();
            try {
                if (op.equals("0")) {
                    return;
                } else if (op.equals("1")) {
                    Optional<Usuario> u = USUARIOS.autenticar(pedir(in, "Usuario: "), pedir(in, "Contraseña: "));
                    if (u.isPresent()) {
                        usuario = u.get();
                    } else {
                        System.out.println("Usuario o contraseña incorrectos.");
                    }
                } else if (op.equals("2")) {
                    boolean creado = USUARIOS.registrar(pedir(in, "Usuario nuevo: "), pedir(in, "Contraseña: "));
                    System.out.println(creado ? "Cuenta creada. Ahora iniciá sesión." : "Ese usuario ya existe.");
                }
            } catch (ColiseumException e) {
                System.out.println("! " + e.getMessage());
            }
        }
        System.out.println("Hola, " + usuario.getNombre() + " (" + usuario.getRol().getEtiqueta() + ")");
        boolean seguir = true;
        while (seguir) {
            System.out.print("\n1) Ver notas de una fecha  2) Agregar nota  3) Editar nota  4) Borrar nota\n"
                    + "5) Reporte  6) Estado del servicio\n"
                    + "7) Ver rutina semanal  8) Guardar un día de la rutina semanal  9) Borrar un día  0) Salir > ");
            String op = in.nextLine().trim();
            try {
                switch (op) {
                    case "1":
                        LocalDate f1 = fecha(in);
                        List<String> notas = RUTINAS.listarNotas(usuario.getId(), f1);
                        System.out.println(notas.isEmpty() ? "(sin notas)" : "");
                        for (int i = 0; i < notas.size(); i++) {
                            System.out.println("  " + (i + 1) + ". " + notas.get(i));
                        }
                        break;
                    case "2":
                        RUTINAS.agregarNota(usuario.getId(), fecha(in), pedir(in, "Nota: "));
                        System.out.println("Nota guardada.");
                        break;
                    case "3":
                        RUTINAS.editarNota(usuario.getId(), fecha(in), pedir(in, "Texto actual: "),
                                pedir(in, "Texto nuevo: "));
                        System.out.println("Nota modificada.");
                        break;
                    case "4":
                        RUTINAS.eliminarNota(usuario.getId(), fecha(in), pedir(in, "Texto exacto de la nota: "));
                        System.out.println("Nota eliminada (si existía).");
                        break;
                    case "5":
                        reporte(usuario);
                        break;
                    case "6":
                        EstadoServicio e = SALUD.verificar();
                        System.out.println((e.isOk() ? "OK" : "ERROR") + " - " + e.getMensaje() + " ["
                                + e.getDestino() + ", " + e.getLatenciaMs() + " ms, SSL: " + (e.isSsl() ? "sí" : "no") + "]");
                        break;
                    case "7":
                        for (RutinaSemanal r : RUTINAS.cargarSemana(usuario.getId())) {
                            System.out.println("  " + nombreDia(r.getDia()) + ": " + r.getResumen()
                                    + (r.getHora() == null ? "" : " (" + r.getHora() + ")"));
                        }
                        break;
                    case "8":
                        DayOfWeek dia = dia(in);
                        Map<DayOfWeek, String> resumen = new EnumMap<>(DayOfWeek.class);
                        Map<DayOfWeek, String> hora = new EnumMap<>(DayOfWeek.class);
                        resumen.put(dia, pedir(in, "Resumen (ej. Piernas): "));
                        hora.put(dia, pedir(in, "Hora HH:mm (vacío = sin hora): "));
                        RUTINAS.guardarSemana(usuario.getId(), resumen, hora); // transacción de 2 tablas
                        System.out.println("Día guardado.");
                        break;
                    case "9":
                        System.out.println(RUTINAS.eliminarDiaSemanal(usuario.getId(), dia(in))
                                ? "Día eliminado." : "Ese día no tenía rutina.");
                        break;
                    case "0":
                        seguir = false;
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (ColiseumException e) {
                System.out.println("! " + e.getMessage());
            }
        }
    }

    private static void reporte(Usuario usuario) throws ColiseumException {
        System.out.println("\nUsuario | Rol | Notas | Días configurados");
        for (FilaReporte f : REPORTES.resumenPorUsuario(usuario)) {
            System.out.println(f.getUsuario() + " | " + f.getRol().getEtiqueta() + " | " + f.getNotas() + " | "
                    + f.getDiasConfigurados());
        }
        System.out.println("\nNotas por mes:");
        for (Map.Entry<String, Long> m : REPORTES.notasPorMes(usuario).entrySet()) {
            System.out.println("  " + m.getKey() + ": " + m.getValue());
        }
        if (usuario.puedeAdministrar()) {
            System.out.printf("%nPromedio de notas por usuario: %.2f%n", REPORTES.promedioNotasPorUsuario());
        }
    }

    private static String nombreDia(DayOfWeek d) {
        return d.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }

    private static DayOfWeek dia(Scanner in) throws ColiseumException {
        String t = pedir(in, "Día (1=lunes ... 7=domingo): ").trim();
        try {
            return DayOfWeek.of(Integer.parseInt(t));
        } catch (RuntimeException e) {
            throw new ColiseumException("El día debe ser un número del 1 al 7.");
        }
    }

    private static String pedir(Scanner in, String texto) {
        System.out.print(texto);
        return in.nextLine();
    }

    private static LocalDate fecha(Scanner in) throws ColiseumException {
        String t = pedir(in, "Fecha (AAAA-MM-DD): ").trim();
        try {
            return LocalDate.parse(t);
        } catch (DateTimeParseException e) {
            throw new ColiseumException("La fecha debe tener el formato AAAA-MM-DD.");
        }
    }
}
