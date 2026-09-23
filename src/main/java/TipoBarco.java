public enum TipoBarco {
    ACORAZADO("Acorazado", 4, 1),
    CRUCERO("Crucero", 3, 2),
    DESTRUCTOR("Destructor", 2, 3),
    SUBMARINO("Submarino", 5, 1);

    public final String nombre;
    public final int longitud;
    public final int cantidad;
    public static final int TOTAL_BARCOS;
    public static final int TOTAL_CASILLAS;

    static {
        int barcos = 0;
        int casillas = 0;
        for (TipoBarco tipo : values()) {
            barcos += tipo.cantidad;
            casillas += tipo.cantidad * tipo.longitud;
        }
        TOTAL_BARCOS = barcos;
        TOTAL_CASILLAS = casillas;
    }

    TipoBarco(String nombre, int longitud, int cantidad) {
        this.nombre = nombre;
        this.longitud = longitud;
        this.cantidad = cantidad;
    }
}
