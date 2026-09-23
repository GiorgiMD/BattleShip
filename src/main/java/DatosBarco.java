public class DatosBarco {
    // Coordenadas de inicio: x = columna, y = fila; ambas entre 1 y 10.
    public final int x;
    public final int y;
    public final int tam;
    public final char orientacion;

    public DatosBarco(int x, int y, int tam, char orientacion) {
        this.x = x;
        this.y = y;
        this.tam = tam;
        this.orientacion = orientacion;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + tam + "," + orientacion;
    }
}
