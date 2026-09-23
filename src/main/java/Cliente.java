import java.net.*;
import java.io.*;

public class Cliente {
    public static void main(String[] args){
        try{
            int pto = 8000;
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in,"ISO-8859-1"));//"Windows-1250"
            InetAddress host = null;
            String dir="";
            try{
                host = InetAddress.getByName("127.0.0.1");
            }catch(Exception n){
                main(args);
            }

            Socket cl = new Socket(host,pto);
            System.out.println("Conexion con el servidor "+dir+":"+pto+" establecida\n");
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream(),"ISO-8859-1"));
            BufferedReader brSocket = new BufferedReader(new InputStreamReader(cl.getInputStream(),"ISO-8859-1"));
            int miTurno = 1;
            while(true){
                if(miTurno == 1){
                    System.out.println("Tu turno, escribe tus coordenadas");
                    String coordenadas = teclado.readLine();
                    if(coordenadas == null) break;
                    pw.println(coordenadas);
                    pw.flush();
                    miTurno--;
                }
                else{
                    String msj = brSocket.readLine();
                    if(msj == null) break;
                    System.out.println("Jugador 2: ("+msj+")");
                    miTurno++;
                }
            }
            cl.close();
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//main
}
