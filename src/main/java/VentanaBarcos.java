import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class VentanaBarcos extends JFrame {
    private static final Color FONDO = new Color(5, 12, 10);
    private static final Color PANEL = new Color(10, 21, 17);
    private static final Color LINEA = new Color(30, 65, 47);
    private static final Color VERDE = new Color(95, 245, 151);
    private static final Color TEXTO = new Color(215, 238, 224);
    private static final Color TENUE = new Color(132, 168, 146);
    private static final Color COLOR_ERROR = new Color(255, 125, 125);
    private final TableroBarcos modelo = new TableroBarcos();
    private final BlockingQueue<DatosBarco> barcosColocados = new LinkedBlockingQueue<>();
    private final List<TarjetaBarco> tarjetas = new ArrayList<>();
    private final PanelTablero tablero = new PanelTablero();
    private final JLabel contador = etiqueta("00 / 07", 26, VERDE);
    private final JLabel ocupacion = etiqueta("0 de 21 casillas ocupadas", 12, TENUE);
    private final JLabel estado = etiqueta("Selecciona un barco o arrástralo al tablero.", 13, TEXTO);
    private final JLabel conexion = etiqueta("MODO LOCAL / COLOCACIÓN", 11, TENUE);
    private final JLabel coordenada = etiqueta("X --  /  Y --", 12, VERDE);
    private final JLabel seleccionActual = etiqueta("SIN BARCO SELECCIONADO", 12, TEXTO);
    private final JToggleButton horizontal = botonOrientacion("Horizontal", true);
    private final JToggleButton vertical = botonOrientacion("Vertical", false);
    private final JProgressBar progreso = new JProgressBar(0, TipoBarco.TOTAL_BARCOS);
    private TarjetaBarco seleccion;
    private char orientacion = 'H';

    public VentanaBarcos() { this("Modo local"); }

    public VentanaBarcos(String jugador) {
        super("BATTLESHIP / " + jugador);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contenido = new JPanel(new BorderLayout(24, 18));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));
        contenido.add(crearCabecera(jugador), BorderLayout.NORTH);
        contenido.add(crearZonaTablero(), BorderLayout.CENTER);
        contenido.add(crearPanelFlota(), BorderLayout.EAST);
        contenido.add(crearPie(), BorderLayout.SOUTH);
        setContentPane(contenido);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotar");
        getRootPane().getActionMap().put("rotar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { cambiarOrientacion(orientacion == 'H' ? 'V' : 'H'); }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelar");
        getRootPane().getActionMap().put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { seleccionar(null); }
        });
        setMinimumSize(new Dimension(900, 720));
        setSize(1100, 820);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel crearCabecera(String jugador) {
        JPanel panel = transparente(new BorderLayout(20, 0));
        JPanel titulos = transparente(new GridLayout(3, 1, 0, 4));
        titulos.add(etiqueta("N A V A L   /   C O M M A N D", 11, VERDE));
        titulos.add(etiqueta("BATTLESHIP", 30, TEXTO));
        titulos.add(etiqueta("01  /  DESPLIEGUE DE FLOTA", 12, TENUE));
        JPanel sesion = transparente(new GridLayout(3, 1, 0, 4));
        sesion.add(etiqueta(jugador.toUpperCase(), 12, VERDE));
        sesion.add(etiqueta("SECTOR 10 × 10", 13, TEXTO));
        sesion.add(etiqueta("7 UNIDADES / 21 CASILLAS", 11, TENUE));
        panel.add(titulos, BorderLayout.WEST);
        panel.add(sesion, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINEA), BorderFactory.createEmptyBorder(0, 0, 18, 0)));
        return panel;
    }

    private JPanel crearZonaTablero() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINEA), BorderFactory.createEmptyBorder(16, 16, 12, 16)));
        JPanel cabecera = transparente(new BorderLayout());
        cabecera.add(etiqueta("TU OCÉANO", 15, TEXTO), BorderLayout.WEST);
        cabecera.add(coordenada, BorderLayout.EAST);
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(tablero, BorderLayout.CENTER);
        JPanel leyenda = transparente(new GridLayout(2, 1, 0, 5));
        leyenda.add(etiqueta("VERDE  posición válida    /    ROJO  posición ocupada o fuera", 11, TENUE));
        leyenda.add(etiqueta("X = columna   Y = fila   ·   Coordenadas de 1 a 10", 11, TENUE));
        panel.add(leyenda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelFlota() {
        JPanel panel = transparente(new BorderLayout(0, 12));
        panel.setPreferredSize(new Dimension(306, 0));
        JPanel resumen = transparente(new BorderLayout(0, 8));
        resumen.add(etiqueta("FLOTA DISPONIBLE", 14, TEXTO), BorderLayout.NORTH);
        resumen.add(contador, BorderLayout.WEST);
        ocupacion.setHorizontalAlignment(SwingConstants.RIGHT);
        resumen.add(ocupacion, BorderLayout.CENTER);
        progreso.setPreferredSize(new Dimension(306, 4));
        progreso.setForeground(VERDE);
        progreso.setBackground(LINEA);
        progreso.setBorderPainted(false);
        resumen.add(progreso, BorderLayout.SOUTH);
        panel.add(resumen, BorderLayout.NORTH);
        JPanel lista = transparente(new GridLayout(TipoBarco.TOTAL_BARCOS, 1, 0, 7));
        for (TipoBarco tipo : TipoBarco.values()) {
            for (int numero = 1; numero <= tipo.cantidad; numero++) {
                TarjetaBarco tarjeta = new TarjetaBarco(tipo, numero);
                tarjetas.add(tarjeta);
                lista.add(tarjeta);
            }
        }
        panel.add(lista, BorderLayout.CENTER);
        JPanel controles = transparente(new GridLayout(3, 1, 0, 6));
        controles.add(seleccionActual);
        JPanel botones = transparente(new GridLayout(1, 2, 8, 0));
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(horizontal);
        grupo.add(vertical);
        horizontal.addActionListener(e -> cambiarOrientacion('H'));
        vertical.addActionListener(e -> cambiarOrientacion('V'));
        botones.add(horizontal);
        botones.add(vertical);
        controles.add(botones);
        controles.add(etiqueta("R  girar antes de colocar  ·  ESC  cancelar", 11, TENUE));
        panel.add(controles, BorderLayout.SOUTH);
        cambiarOrientacion('H');
        return panel;
    }

    private JPanel crearPie() {
        JPanel pie = transparente(new GridLayout(2, 1, 0, 6));
        pie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LINEA), BorderFactory.createEmptyBorder(12, 0, 0, 0)));
        pie.add(estado);
        pie.add(conexion);
        return pie;
    }

    private void seleccionar(TarjetaBarco tarjeta) {
        if (tarjeta != null && tarjeta.colocado) return;
        seleccion = tarjeta;
        seleccionActual.setText(tarjeta == null ? "SIN BARCO SELECCIONADO" : tarjeta.tipo.nombre.toUpperCase() + " / " + tarjeta.tipo.longitud + " CASILLAS");
        mostrarEstado(tarjeta == null ? "Selecciona un barco o arrástralo al tablero." : "Coloca el " + tarjeta.tipo.nombre.toLowerCase() + ": arrastra o haz clic en su casilla de inicio.", false);
        tarjetas.forEach(Component::repaint);
        tablero.repaint();
    }

    private void cambiarOrientacion(char nueva) {
        orientacion = nueva;
        horizontal.setSelected(nueva == 'H');
        vertical.setSelected(nueva == 'V');
        horizontal.setBackground(nueva == 'H' ? LINEA : PANEL);
        vertical.setBackground(nueva == 'V' ? LINEA : PANEL);
        horizontal.setForeground(nueva == 'H' ? VERDE : TENUE);
        vertical.setForeground(nueva == 'V' ? VERDE : TENUE);
        tablero.repaint();
    }

    private boolean colocarBarco(Point celda) {
        if (seleccion == null) {
            mostrarEstado("Primero selecciona un barco de la flota.", true);
            return false;
        }
        if (celda == null) {
            mostrarEstado("Suelta el barco dentro de la cuadrícula.", true);
            return false;
        }
        String error = modelo.validarColocacion(seleccion.tipo, celda.x + 1, celda.y + 1, orientacion);
        if (error != null) {
            mostrarEstado(error, true);
            return false;
        }
        DatosBarco barco = modelo.colocar(seleccion.tipo, celda.x + 1, celda.y + 1, orientacion);
        String nombre = seleccion.tipo.nombre;
        seleccion.colocado = true;
        seleccion.setEnabled(false);
        seleccion.setToolTipText(nombre + " colocado en " + barco);
        seleccion = null;
        tablero.previa = null;
        seleccionActual.setText("SIN BARCO SELECCIONADO");
        // Aquí salen los cuatro datos hacia esperarBarco(), usado por Cliente/Servidor.
        barcosColocados.offer(barco);
        int cantidad = modelo.obtenerBarcos().size();
        int casillas = modelo.obtenerPosiciones().stream().mapToInt(datos -> datos.tam).sum();
        contador.setText(String.format("%02d / %02d", cantidad, TipoBarco.TOTAL_BARCOS));
        ocupacion.setText(casillas + " de " + TipoBarco.TOTAL_CASILLAS + " casillas ocupadas");
        progreso.setValue(cantidad);
        mostrarEstado(cantidad == TipoBarco.TOTAL_BARCOS ? "Flota desplegada. Las 7 unidades están en posición." : nombre + " colocado · X " + barco.x + " / Y " + barco.y + " / " + barco.tam + " casillas / " + barco.orientacion, false);
        tarjetas.forEach(Component::repaint);
        tablero.repaint();
        return true;
    }

    private void mostrarEstado(String mensaje, boolean error) {
        estado.setText(mensaje);
        estado.setForeground(error ? COLOR_ERROR : TEXTO);
    }

    public void mostrarConexion(String mensaje) {
        SwingUtilities.invokeLater(() -> conexion.setText(mensaje));
    }

    // Debe llamarse desde el hilo de red, nunca desde un evento de Swing.
    public DatosBarco esperarBarco() {
        if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("esperarBarco() bloquearía la interfaz; úsalo desde el hilo de red.");
        try {
            return barcosColocados.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    // Conserva las posiciones aunque Cliente/Servidor ya las hayan retirado de la cola.
    public List<DatosBarco> obtenerBarcosColocados() { return modelo.obtenerPosiciones(); }

    public int[][] obtenerMatriz() { return modelo.obtenerMatriz(); }

    public static VentanaBarcos abrir(String jugador) throws InvocationTargetException, InterruptedException {
        if (SwingUtilities.isEventDispatchThread()) return new VentanaBarcos(jugador);
        AtomicReference<VentanaBarcos> ventana = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ventana.set(new VentanaBarcos(jugador)));
        return ventana.get();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaBarcos::new);
    }

    private static JLabel etiqueta(String texto, int tamano, Color color) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, tamano));
        label.setForeground(color);
        return label;
    }

    private static JPanel transparente(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JToggleButton botonOrientacion(String texto, boolean seleccionado) {
        JToggleButton boton = new JToggleButton(texto, seleccionado);
        boton.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINEA), BorderFactory.createEmptyBorder(7, 4, 7, 4)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private final class TarjetaBarco extends JButton {
        private final TipoBarco tipo;
        private final int numero;
        private final String id = UUID.randomUUID().toString();
        private boolean colocado;

        TarjetaBarco(TipoBarco tipo, int numero) {
            this.tipo = tipo;
            this.numero = numero;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Arrastra " + tipo.nombre + " al tablero: " + tipo.longitud + " casillas. También puedes seleccionarlo y hacer clic.");
            getAccessibleContext().setAccessibleName(tipo.nombre + " " + numero + ", " + tipo.longitud + " casillas");
            addActionListener(e -> seleccionar(this));
            setTransferHandler(new TransferHandler() {
                @Override
                public int getSourceActions(JComponent c) { return COPY; }

                @Override
                protected Transferable createTransferable(JComponent c) { return colocado ? null : new StringSelection(id); }

                @Override
                protected void exportDone(JComponent source, Transferable data, int action) {
                    tablero.previa = null;
                    tablero.repaint();
                }
            });
            MouseAdapter arrastre = new MouseAdapter() {
                private Point inicio;

                @Override
                public void mousePressed(MouseEvent e) {
                    inicio = SwingUtilities.isLeftMouseButton(e) && !colocado ? e.getPoint() : null;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (inicio == null || colocado || inicio.distance(e.getPoint()) < 5) return;
                    inicio = null;
                    seleccionar(TarjetaBarco.this);
                    getTransferHandler().exportAsDrag(TarjetaBarco.this, e, TransferHandler.COPY);
                }

                @Override
                public void mouseReleased(MouseEvent e) { inicio = null; }
            };
            addMouseListener(arrastre);
            addMouseMotionListener(arrastre);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean activa = seleccion == this;
            g.setColor(activa ? new Color(17, 47, 31) : PANEL);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g.setColor(activa || hasFocus() || getModel().isRollover() && !colocado ? VERDE : LINEA);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            if (colocado) g.setComposite(AlphaComposite.SrcOver.derive(0.45f));
            int anchoImagen = 57 + tipo.longitud * 10;
            new IconoBarco(tipo, anchoImagen, 32, VERDE).paintIcon(this, g, 8 + (108 - anchoImagen) / 2, (getHeight() - 32) / 2);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            g.setColor(TEXTO);
            g.drawString(tipo.nombre + (tipo.cantidad > 1 ? " 0" + numero : ""), 124, getHeight() / 2 - 3);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            g.setColor(colocado ? VERDE : TENUE);
            g.drawString(colocado ? "EN POSICIÓN" : tipo.longitud + " casillas", 124, getHeight() / 2 + 14);
            g.setColor(VERDE);
            if (!colocado) {
                for (int i = 0; i < tipo.longitud; i++) g.fillRect(getWidth() - 14 - i * 6, getHeight() - 12, 3, 3);
            }
            g.dispose();
        }
    }

    private final class PanelTablero extends JPanel {
        private Point previa;

        PanelTablero() {
            setOpaque(false);
            setPreferredSize(new Dimension(580, 560));
            setToolTipText("Arrastra un barco o selecciona uno y haz clic. La casilla elegida es su inicio.");
            MouseAdapter raton = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) { actualizarPrevia(e.getPoint()); }

                @Override
                public void mouseExited(MouseEvent e) {
                    previa = null;
                    coordenada.setText("X --  /  Y --");
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) colocarBarco(celdaEn(e.getPoint()));
                }
            };
            addMouseListener(raton);
            addMouseMotionListener(raton);
            setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferSupport support) {
                    if (!support.isDrop() || !support.isDataFlavorSupported(DataFlavor.stringFlavor)) return false;
                    TarjetaBarco tarjeta = tarjetaTransferida(support);
                    if (tarjeta == null || tarjeta.colocado) return false;
                    if (seleccion != tarjeta) seleccionar(tarjeta);
                    actualizarPrevia(support.getDropLocation().getDropPoint());
                    return previa != null && modelo.validarColocacion(tarjeta.tipo, previa.x + 1, previa.y + 1, orientacion) == null;
                }

                @Override
                public boolean importData(TransferSupport support) {
                    if (!canImport(support)) return false;
                    return colocarBarco(celdaEn(support.getDropLocation().getDropPoint()));
                }
            });
        }

        private TarjetaBarco tarjetaTransferida(TransferHandler.TransferSupport support) {
            try {
                String id = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                return tarjetas.stream().filter(tarjeta -> tarjeta.id.equals(id)).findFirst().orElse(null);
            } catch (java.awt.datatransfer.UnsupportedFlavorException | IOException e) {
                return null;
            }
        }

        private Rectangle areaTablero() {
            int casilla = Math.max(1, Math.min((getWidth() - 44) / TableroBarcos.LADO, (getHeight() - 34) / TableroBarcos.LADO));
            int lado = casilla * TableroBarcos.LADO;
            return new Rectangle((getWidth() - lado + 24) / 2, (getHeight() - lado + 24) / 2, lado, lado);
        }

        private Point celdaEn(Point punto) {
            Rectangle area = areaTablero();
            if (!area.contains(punto)) return null;
            int casilla = area.width / TableroBarcos.LADO;
            return new Point((punto.x - area.x) / casilla, (punto.y - area.y) / casilla);
        }

        private void actualizarPrevia(Point punto) {
            previa = celdaEn(punto);
            coordenada.setText(previa == null ? "X --  /  Y --" : String.format("X %02d  /  Y %02d", previa.x + 1, previa.y + 1));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle area = areaTablero();
            int casilla = area.width / TableroBarcos.LADO;
            g.setColor(FONDO);
            g.fillRect(area.x, area.y, area.width, area.height);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            for (int i = 0; i < TableroBarcos.LADO; i++) {
                String numero = String.format("%02d", i + 1);
                g.setColor(TENUE);
                g.drawString(numero, area.x + i * casilla + (casilla - g.getFontMetrics().stringWidth(numero)) / 2, area.y - 12);
                g.drawString(numero, area.x - 29, area.y + i * casilla + casilla / 2 + 4);
                for (int j = 0; j < TableroBarcos.LADO; j++) {
                    g.setColor(new Color(7, (i + j) % 2 == 0 ? 20 : 24, 15));
                    g.fillRect(area.x + i * casilla + 1, area.y + j * casilla + 1, casilla - 1, casilla - 1);
                    g.setColor(LINEA);
                    g.fillRect(area.x + i * casilla + casilla / 2, area.y + j * casilla + casilla / 2, 2, 2);
                }
            }
            g.setColor(LINEA);
            for (int i = 0; i <= TableroBarcos.LADO; i++) {
                g.drawLine(area.x + i * casilla, area.y, area.x + i * casilla, area.y + area.height);
                g.drawLine(area.x, area.y + i * casilla, area.x + area.width, area.y + i * casilla);
            }
            for (TableroBarcos.BarcoColocado barco : modelo.obtenerBarcos()) dibujarBarco(g, area, casilla, barco.tipo(), barco.datos(), VERDE, false);
            if (seleccion != null && previa != null) {
                DatosBarco datos = new DatosBarco(previa.x + 1, previa.y + 1, seleccion.tipo.longitud, orientacion);
                boolean valida = modelo.validarColocacion(seleccion.tipo, datos.x, datos.y, orientacion) == null;
                Graphics2D preview = (Graphics2D) g.create();
                preview.clip(area);
                dibujarBarco(preview, area, casilla, seleccion.tipo, datos, valida ? VERDE : COLOR_ERROR, true);
                preview.dispose();
            }
            g.setColor(VERDE);
            int borde = 12;
            g.drawLine(area.x, area.y, area.x + borde, area.y);
            g.drawLine(area.x, area.y, area.x, area.y + borde);
            g.drawLine(area.x + area.width, area.y + area.height, area.x + area.width - borde, area.y + area.height);
            g.drawLine(area.x + area.width, area.y + area.height, area.x + area.width, area.y + area.height - borde);
            g.dispose();
        }

        private void dibujarBarco(Graphics2D g, Rectangle area, int casilla, TipoBarco tipo, DatosBarco datos, Color color, boolean preview) {
            int x = area.x + (datos.x - 1) * casilla;
            int y = area.y + (datos.y - 1) * casilla;
            int ancho = casilla * (datos.orientacion == 'H' ? datos.tam : 1);
            int alto = casilla * (datos.orientacion == 'V' ? datos.tam : 1);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), preview ? 38 : 20));
            g.fillRect(x + 2, y + 2, ancho - 4, alto - 4);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 130));
            g.drawRect(x + 3, y + 3, ancho - 6, alto - 6);
            IconoBarco.dibujar(g, tipo, x + 5, y + 5, ancho - 10, alto - 10, datos.orientacion, color);
        }
    }
}
