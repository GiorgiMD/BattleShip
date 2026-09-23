import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.Path2D;

// Ilustraciones vectoriales: se ven nítidas al cambiar el tamaño de la ventana.
public class IconoBarco implements Icon {
    private final TipoBarco tipo;
    private final int ancho;
    private final int alto;
    private final Color color;

    public IconoBarco(TipoBarco tipo, int ancho, int alto, Color color) {
        this.tipo = tipo;
        this.ancho = ancho;
        this.alto = alto;
        this.color = color;
    }

    @Override
    public int getIconWidth() { return ancho; }

    @Override
    public int getIconHeight() { return alto; }

    @Override
    public void paintIcon(Component componente, Graphics graphics, int x, int y) {
        dibujar((Graphics2D) graphics, tipo, x, y, ancho, alto, 'H', color);
    }

    public static void dibujar(Graphics2D graphics, TipoBarco tipo, int x, int y, int ancho, int alto, char orientacion, Color color) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(x, y);
        if (orientacion == 'V') {
            g.translate(ancho, 0);
            g.rotate(Math.PI / 2);
            g.scale(alto / 200.0, ancho / 48.0);
        } else {
            g.scale(ancho / 200.0, alto / 48.0);
        }
        g.setStroke(new BasicStroke(1.5f));
        if (tipo == TipoBarco.SUBMARINO) {
            g.setColor(new Color(16, 63, 43));
            g.fillRoundRect(13, 11, 173, 26, 30, 30);
            g.setColor(color);
            g.drawRoundRect(13, 11, 173, 26, 30, 30);
            g.drawLine(8, 14, 8, 34);
            g.drawLine(5, 24, 20, 24);
            g.drawLine(40, 5, 40, 43);
            g.drawLine(132, 5, 132, 43);
            g.setColor(new Color(38, 114, 73));
            g.fillRoundRect(77, 15, 39, 18, 8, 8);
            g.setColor(color);
            g.drawRoundRect(77, 15, 39, 18, 8, 8);
            g.drawLine(93, 18, 104, 18);
            g.drawLine(97, 17, 97, 29);
            g.drawLine(150, 17, 169, 20);
            g.drawLine(150, 31, 169, 28);
        } else {
            Path2D casco = new Path2D.Double();
            casco.moveTo(12, 12);
            casco.lineTo(148, 9);
            casco.curveTo(170, 10, 184, 17, 193, 24);
            casco.curveTo(184, 31, 170, 38, 148, 39);
            casco.lineTo(12, 36);
            casco.closePath();
            g.setColor(new Color(17, 62, 42));
            g.fill(casco);
            g.setColor(color);
            g.draw(casco);
            g.setColor(new Color(28, 92, 58));
            g.fillRoundRect(66, 13, 54, 22, 5, 5);
            g.setColor(color);
            g.drawRoundRect(66, 13, 54, 22, 5, 5);
            g.drawRect(91, 17, 20, 14);
            g.drawLine(103, 10, 103, 38);
            g.drawLine(96, 24, 111, 24);
            g.drawRect(72, 18, 10, 12);
            torreta(g, 141, tipo == TipoBarco.ACORAZADO ? 3 : 2, color);
            if (tipo != TipoBarco.DESTRUCTOR) torreta(g, 42, 2, color);
            if (tipo == TipoBarco.ACORAZADO) torreta(g, 163, 2, color);
            g.drawLine(20, 16, 28, 16);
            g.drawLine(20, 32, 28, 32);
            g.drawLine(7, 18, 12, 18);
            g.drawLine(7, 30, 12, 30);
        }
        g.dispose();
    }

    private static void torreta(Graphics2D g, int x, int canones, Color color) {
        g.setColor(new Color(38, 114, 73));
        g.fillOval(x - 7, 17, 14, 14);
        g.setColor(color);
        g.drawOval(x - 7, 17, 14, 14);
        for (int i = 0; i < canones; i++) g.drawLine(x, 21 + i * 3, x + 17, 21 + i * 3);
    }
}
