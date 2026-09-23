import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Servidor {
    int[][] matriz = new int[10][10];

    public void AcomodarBarcos(int x, int y) {
        matriz[y - 1][x - 1] = 1;
        for (int[] fila : matriz) {
            for (int col : fila) System.out.print(col + " ");
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int pto = 8000;
        try (ServerSocket servidor = new ServerSocket(pto)) {
            System.out.println("Servidor iniciado en el puerto " + pto + "... esperando cliente...");
            while (true) {
                VentanaBarcos ventana = null;
                try (Socket cl = servidor.accept();
                     PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream(), StandardCharsets.ISO_8859_1), true);
                     BufferedReader brSocket = new BufferedReader(new InputStreamReader(cl.getInputStream(), StandardCharsets.ISO_8859_1))) {
                    System.out.println("Cliente conectado desde " + cl.getInetAddress() + ":" + cl.getPort());
                    ventana = VentanaBarcos.abrir("Jugador 2 / Servidor");
                    for (int i = 0; i < TipoBarco.TOTAL_BARCOS; i++) {
                        ventana.mostrarConexion("CONECTADO / Coloca tu flota. Intercambio " + (i + 1) + " de " + TipoBarco.TOTAL_BARCOS);
                        String msj = brSocket.readLine();
                        if (msj == null) throw new EOFException("El cliente cerró la conexión.");
                        System.out.println("Jugador 1: (" + msj + ")");
                        DatosBarco barco = ventana.esperarBarco();
                        if (barco == null) return;
                        // AQUÍ OBTIENES LOS CUATRO DATOS del barco que acabas de colocar.
                        int x = barco.x;
                        int y = barco.y;
                        int tam = barco.tam;
                        char orientacion = barco.orientacion;
                        System.out.println("x = " + x + ", y = " + y + ", tam = " + tam + ", orientación = " + orientacion);
                        pw.println(barco); // Envía x,y,tam,orientacion; autoFlush está activado.
                        if (pw.checkError()) throw new IOException("No se pudo enviar el barco al cliente.");
                    }
                    System.out.println("Los 7 barcos de cada jugador fueron intercambiados.");
                    ventana.faseAtaques = true;
                    
                    // --- INICIO FASE DE ATAQUES ---
                    int aciertosServidor = 0;
                    int aciertosCliente = 0;
                    boolean turnoCliente = true; 
                    java.util.Random rand = new java.util.Random();
                    boolean[][] tirosServidor = new boolean[10][10];
                    int[][] miMatriz = ventana.obtenerMatriz();

                    ventana.mostrarConexion("FASE DE ATAQUES / ESPERANDO ATAQUE DEL JUGADOR 1");

                    while (aciertosCliente < TipoBarco.TOTAL_CASILLAS && aciertosServidor < TipoBarco.TOTAL_CASILLAS) {
                        if (turnoCliente) {
                            String msj = brSocket.readLine();
                            if (msj == null) throw new EOFException("El cliente cerró la conexión.");

                            String[] partes = msj.split(",");
                            int x = Integer.parseInt(partes[0]);
                            int y = Integer.parseInt(partes[1]);
                            
                            ventana.registrarAtaqueEnemigo(x, y); // El servidor ve dónde le ataca el cliente

                            if (miMatriz[y - 1][x - 1] == 1) { 
                                miMatriz[y - 1][x - 1] = 2; 
                                aciertosCliente++;
                                if (aciertosCliente == TipoBarco.TOTAL_CASILLAS) {
                                    pw.println("WIN");
                                    ventana.mostrarConexion("DERROTA / El Jugador 1 destruyó tu flota.");
                                    break;
                                } else {
                                    pw.println("HIT");
                                }
                            } else {
                                pw.println("MISS");
                                turnoCliente = false;
                                ventana.mostrarConexion("FASE DE ATAQUES / CALCULANDO TIRO AUTOMÁTICO...");
                                Thread.sleep(700); // Pausa visual breve para no ser inmediato
                            }
                        } else {
                            int x, y;
                            do {
                                x = rand.nextInt(10) + 1;
                                y = rand.nextInt(10) + 1;
                            } while (tirosServidor[y - 1][x - 1]);
                            tirosServidor[y - 1][x - 1] = true;

                            pw.println(x + "," + y);
                            String msj = brSocket.readLine();
                            if (msj == null) throw new EOFException("El cliente cerró la conexión.");

                            if (msj.equals("HIT")) {
                                aciertosServidor++;
                                ventana.registrarMiAtaque(x, y, true);
                                ventana.mostrarConexion("¡IMPACTO DEL SERVIDOR! / Tirando de nuevo...");
                                Thread.sleep(700);
                            } else if (msj.equals("WIN")) {
                                ventana.registrarMiAtaque(x, y, true);
                                ventana.mostrarConexion("VICTORIA / Has destruido la flota del Jugador 1.");
                                break;
                            } else {
                                ventana.registrarMiAtaque(x, y, false);
                                turnoCliente = true;
                                ventana.mostrarConexion("FASE DE ATAQUES / ESPERANDO ATAQUE DEL JUGADOR 1");
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ventana != null) ventana.mostrarConexion("CONEXIÓN INTERRUMPIDA / " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
