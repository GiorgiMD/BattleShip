import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Cliente {
    public static void main(String[] args) {
        int pto = 8000;
        String dir = args.length > 0 ? args[0] : "127.0.0.1";
        VentanaBarcos ventana = null;
        try (Socket cl = new Socket(dir, pto);
             PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream(), StandardCharsets.ISO_8859_1), true);
             BufferedReader brSocket = new BufferedReader(new InputStreamReader(cl.getInputStream(), StandardCharsets.ISO_8859_1))) {
            System.out.println("Conexión con el servidor " + dir + ":" + pto + " establecida");
            ventana = VentanaBarcos.abrir("Jugador 1 / Cliente");
            for (int i = 0; i < TipoBarco.TOTAL_BARCOS; i++) {
                ventana.mostrarConexion("CONECTADO / Coloca tu flota. Intercambio " + (i + 1) + " de " + TipoBarco.TOTAL_BARCOS);
                DatosBarco barco = ventana.esperarBarco();
                if (barco == null) return;
                // AQUÍ OBTIENES LOS CUATRO DATOS del barco que acabas de colocar.
                int x = barco.x;
                int y = barco.y;
                int tam = barco.tam;
                char orientacion = barco.orientacion;
                System.out.println("x = " + x + ", y = " + y + ", tam = " + tam + ", orientación = " + orientacion);
                pw.println(barco); // Envía x,y,tam,orientacion; autoFlush está activado.
                if (pw.checkError()) throw new IOException("No se pudo enviar el barco al servidor.");
                String msj = brSocket.readLine();
                if (msj == null) throw new EOFException("El servidor cerró la conexión.");
                System.out.println("Jugador 2: (" + msj + ")");
            }
            System.out.println("Los 7 barcos de cada jugador fueron intercambiados.");
            ventana.mostrarConexion("FLOTAS INTERCAMBIADAS / Colocación terminada. Fase de ataques pendiente.");
            // La fase de ataques debe ir AQUÍ, antes de salir del try y cerrar el socket.
        } catch (Exception e) {
            if (ventana != null) ventana.mostrarConexion("CONEXIÓN INTERRUMPIDA / " + e.getMessage());
            e.printStackTrace();
        }
    }
}
