
import java.net.*;
import java.io.*;


public class Cliente {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int pto = 5000;

        try (Socket cl = new Socket(host, pto);
             PrintWriter pw = new PrintWriter(cl.getOutputStream());
             BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
             BufferedReader brConsola = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conexión con el servidor establecida.");

            while (true) {
                System.out.print("Escribe la fila (o 'salir' para terminar): ");
                String entradaFila = brConsola.readLine(); 

                if (entradaFila == null || entradaFila.equalsIgnoreCase("salir")) {
                    pw.println("salir");
                    pw.flush();
                    break;
                }

                System.out.print("Escribe la columna: ");
                String entradaColumna = brConsola.readLine(); 

                pw.println(entradaFila + "," + entradaColumna);
                pw.flush();
                System.out.println("Coordenadas enviadas.");

                String respuesta = br.readLine();
                System.out.println("Respuesta del servidor: " + respuesta);
            }
            
            System.out.println("Cliente cierra conexión");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}