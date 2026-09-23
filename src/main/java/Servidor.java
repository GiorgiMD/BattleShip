import java.net.*;
import java.io.*;

public class Servidor{
    public static void main(String[] args){
        try{
            int pto = 8000;
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in,"ISO-8859-1"));
            ServerSocket s = new ServerSocket(pto);
            System.out.println("Servidor iniciado en el puerto "+pto+" .. esperando cliente..");
            for(;;){
                Socket cl = s.accept();
                System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream(),"ISO-8859-1"));
                BufferedReader brSocket = new BufferedReader(new InputStreamReader(cl.getInputStream(),"ISO-8859-1"));
                int miTurno = 0;
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
                        System.out.println("Jugador 1: ("+msj+")");
                        miTurno++;
                    }
                }
                cl.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
