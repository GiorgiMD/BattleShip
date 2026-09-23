import java.util.ArrayList;
import java.util.List;

public class TableroBarcos {
    public static final int LADO = 10;
    private final int[][] matriz = new int[LADO][LADO];
    private final List<BarcoColocado> barcos = new ArrayList<>();

    public record BarcoColocado(TipoBarco tipo, DatosBarco datos) {}

    // Retorna null cuando la posición es válida. No modifica el tablero.
    public synchronized String validarColocacion(TipoBarco tipo, int x, int y, char orientacion) {
        if (tipo == null) return "Selecciona un barco de la flota.";
        if (orientacion != 'H' && orientacion != 'V') return "La orientación debe ser H o V.";
        if (barcos.stream().filter(barco -> barco.tipo() == tipo).count() >= tipo.cantidad) return "Ya colocaste todos los barcos de este tipo.";
        int finX = x + (orientacion == 'H' ? tipo.longitud - 1 : 0);
        int finY = y + (orientacion == 'V' ? tipo.longitud - 1 : 0);
        if (x < 1 || y < 1 || x > LADO || y > LADO || finX > LADO || finY > LADO) return "El barco debe quedar dentro del tablero.";
        for (int i = 0; i < tipo.longitud; i++) {
            int fila = y - 1 + (orientacion == 'V' ? i : 0);
            int columna = x - 1 + (orientacion == 'H' ? i : 0);
            if (matriz[fila][columna] != 0) return "Hay otro barco en esas casillas.";
        }
        return null;
    }

    public synchronized DatosBarco colocar(TipoBarco tipo, int x, int y, char orientacion) {
        String error = validarColocacion(tipo, x, y, orientacion);
        if (error != null) throw new IllegalArgumentException(error);
        DatosBarco datos = new DatosBarco(x, y, tipo.longitud, orientacion);
        for (int i = 0; i < datos.tam; i++) {
            int fila = y - 1 + (orientacion == 'V' ? i : 0);
            int columna = x - 1 + (orientacion == 'H' ? i : 0);
            matriz[fila][columna] = 1;
        }
        barcos.add(new BarcoColocado(tipo, datos));
        return datos;
    }

    public synchronized List<BarcoColocado> obtenerBarcos() {
        return List.copyOf(barcos);
    }

    public synchronized List<DatosBarco> obtenerPosiciones() {
        return barcos.stream().map(BarcoColocado::datos).toList();
    }

    public synchronized int[][] obtenerMatriz() {
        int[][] copia = new int[LADO][];
        for (int y = 0; y < LADO; y++) copia[y] = matriz[y].clone();
        return copia;
    }
}
