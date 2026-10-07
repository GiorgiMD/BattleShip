import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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
            ventana.faseAtaques = true; // Activa los clics para atacar en la GUI
            
            // --- INICIO FASE DE ATAQUES ---
            int aciertosCliente = 0;
            int aciertosServidor = 0;
            boolean turnoCliente = true; 
            int[][] miMatriz = ventana.obtenerMatriz();

            ventana.mostrarConexion("FASE DE ATAQUES / TU TURNO (Haz clic en la cuadrícula)");

            while (aciertosCliente < TipoBarco.TOTAL_CASILLAS && aciertosServidor < TipoBarco.TOTAL_CASILLAS) {
                if (turnoCliente) {
                    java.awt.Point p = ventana.esperarAtaque(); // Espera a que el usuario haga clic
                    if (p == null) break;
                    
                    pw.println(p.x + "," + p.y); 
                    if (pw.checkError()) throw new IOException("No se pudo enviar ataque.");

                    String msj = brSocket.readLine();
                    if (msj == null) throw new EOFException("El servidor cerró la conexión.");

                    if (msj.equals("HIT")) {
                        aciertosCliente++;
                        ventana.registrarMiAtaque(p.x, p.y, true);
                        ventana.mostrarConexion("¡IMPACTO! / Vuelve a tirar (Haz clic)");
                    } else if (msj.equals("WIN")) {
                        ventana.registrarMiAtaque(p.x, p.y, true);
                        ventana.mostrarFinPartida(true, "Jugador 1 / Cliente");
                        break;
                    } else {
                        ventana.registrarMiAtaque(p.x, p.y, false);
                        turnoCliente = false;
                        ventana.mostrarConexion("FASE DE ATAQUES / AGUA... TURNO ENEMIGO");
                    }
                } else {
                    String msj = brSocket.readLine();
                    if (msj == null) throw new EOFException("El servidor cerró la conexión.");

                    String[] partes = msj.split(",");
                    int x = Integer.parseInt(partes[0]);
                    int y = Integer.parseInt(partes[1]);
                    
                    ventana.registrarAtaqueEnemigo(x, y); // Dibuja la X naranja del enemigo

                    if (miMatriz[y - 1][x - 1] == 1) { 
                        miMatriz[y - 1][x - 1] = 2; 
                        aciertosServidor++;
                        if (aciertosServidor == TipoBarco.TOTAL_CASILLAS) {
                            pw.println("WIN");
                            ventana.mostrarFinPartida(false, "Jugador 2 / Servidor");
                            break;
                        } else {
                            pw.println("HIT");
                        }
                    } else {
                        pw.println("MISS");
                        turnoCliente = true;
                        ventana.mostrarConexion("FASE DE ATAQUES / EL ENEMIGO FALLÓ, TU TURNO (Haz clic)");
                    }
                }
            }
        } catch (Exception e) {
            if (ventana != null) ventana.mostrarConexion("CONEXIÓN INTERRUMPIDA / " + e.getMessage());
            e.printStackTrace();
        }
    }
}
