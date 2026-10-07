from pathlib import Path
import textwrap
import zipfile

from PIL import Image, ImageDraw, ImageFont
from pygments import lex
from pygments.lexers import JavaLexer
from pygments.token import Comment, Keyword, Literal, Name, Operator


OUTPUT = Path(__file__).resolve().parent
ROOT = OUTPUT.parent
SOURCE = ROOT / "src" / "main" / "java"
FONT = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 24)
SMALL = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 20)
TITLE = ImageFont.truetype(r"C:\Windows\Fonts\consolab.ttf", 30)
WIDTH = 1570
MAX_CHARS = 96
LINE_HEIGHT = 34
CHAR_WIDTH = FONT.getlength("M")

FIGURES = [
    ("01_conexion.png", "Conexión cliente-servidor", [("Servidor.java", 17, 26), ("Cliente.java", 7, 14)],
     "Lógica de comunicación: después de explicar ServerSocket, Socket y los flujos de entrada y salida.",
     "Figura 1. Establecimiento de la conexión TCP y configuración de los flujos de comunicación."),
    ("02_intercambio_barcos.png", "Formato e intercambio de embarcaciones", [("DatosBarco.java", 15, 18), ("Cliente.java", 16, 31)],
     "Lógica de comunicación: después de explicar el formato x,y,tam,orientacion y los siete intercambios.",
     "Figura 2. Conversión de los datos del barco a texto y envío y recepción durante la colocación de la flota."),
    ("03_ataques_turnos.png", "Envío de ataques y control del turno", [("Cliente.java", 43, 66)],
     "Lógica de comunicación: después de la tabla HIT, MISS y WIN. También ilustra la regla de repetición del turno en la lógica del juego.",
     "Figura 3. Envío de coordenadas, recepción del resultado y actualización del turno del cliente."),
    ("04_flota.png", "Composición de la flota", [("TipoBarco.java", 1, 22)],
     "Lógica del juego: después de la tabla de embarcaciones y sus tamaños.",
     "Figura 4. Definición de los tipos de barco y cálculo de las siete embarcaciones y las 21 casillas."),
    ("05_colocacion.png", "Validación y colocación de barcos", [("TableroBarcos.java", 11, 38)],
     "Lógica del juego: después de explicar la validación de posiciones y el registro de casillas ocupadas.",
     "Figura 5. Validación de límites, disponibilidad y superposición, y registro del barco en la matriz."),
    ("06_impactos_victoria.png", "Evaluación de impactos y fin de partida", [("Servidor.java", 57, 83)],
     "Lógica del juego: después de explicar los estados de la matriz y la condición de victoria.",
     "Figura 6. Evaluación del disparo recibido, registro del daño y envío de HIT, MISS o WIN."),
    ("07_ataques_servidor.png", "Disparos automáticos del servidor", [("Servidor.java", 85, 94)],
     "Lógica del juego: después del párrafo sobre coordenadas aleatorias y la matriz tirosServidor.",
     "Figura 7. Generación de disparos aleatorios sin repetición y envío de coordenadas al cliente."),
]


def token_color(token):
    if token in Comment:
        return "#52647B"
    if token in Keyword:
        return "#7942B5"
    if token in Literal.String:
        return "#126D49"
    if token in Literal.Number:
        return "#A34F12"
    if token in Name.Class or token in Name.Function:
        return "#1455A0"
    if token in Operator:
        return "#586278"
    return "#17283F"


def prepare_rows(filename, start, end):
    lines = (SOURCE / filename).read_text(encoding="utf-8-sig").splitlines()
    assert 1 <= start <= end <= len(lines)
    original = lines[start - 1:end]
    normalized = textwrap.dedent("\n".join(original)).split("\n")
    rows = []
    for number, line in enumerate(normalized, start):
        chars = [(char, token_color(token))
                 for token, value in lex(line + "\n", JavaLexer(stripnl=False, ensurenl=False))
                 for char in value if char != "\n"]
        assert "".join(char for char, _ in chars) == line
        chunks = []
        while len(chars) > MAX_CHARS:
            cut = MAX_CHARS
            for i in range(MAX_CHARS - 1, MAX_CHARS // 2, -1):
                if chars[i][0] in " ,":
                    cut = i + 1
                    break
            chunks.append(chars[:cut])
            chars = chars[cut:]
        chunks.append(chars)
        assert "".join(char for chunk in chunks for char, _ in chunk) == line
        for index, chunk in enumerate(chunks):
            rows.append((str(number) if index == 0 else ">", chunk))
    return rows


def render(filename, title, selections):
    blocks = [(name, start, end, prepare_rows(name, start, end))
              for name, start, end in selections]
    height = 94 + sum(60 + len(rows) * LINE_HEIGHT + 20 for _, _, _, rows in blocks) + 46
    image = Image.new("RGB", (WIDTH, height), "#FFFFFF")
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, WIDTH, 8), fill="#1D669B")
    draw.text((30, 30), title, font=TITLE, fill="#17283F")
    y = 94
    for name, start, end, rows in blocks:
        block_end = y + 60 + len(rows) * LINE_HEIGHT + 8
        draw.rounded_rectangle((22, y, WIDTH - 22, block_end), radius=10, fill="#F5F7FA", outline="#DCE2EB")
        label = f"src/main/java/{name}  |  líneas {start}–{end}"
        draw.text((38, y + 15), label, font=SMALL, fill="#31516F")
        code_y = y + 60
        draw.line((102, code_y - 5, 102, block_end - 10), fill="#DCE2EB", width=1)
        for number, chars in rows:
            draw.text((88, code_y), number, font=SMALL, anchor="ra", fill="#6D7C90")
            for column, (char, color) in enumerate(chars):
                draw.text((120 + column * CHAR_WIDTH, code_y), char, font=FONT, fill=color)
            code_y += LINE_HEIGHT
        y = block_end + 12
    draw.text((30, height - 34), "Código fuente del proyecto · > indica continuación de la misma línea", font=SMALL, fill="#52647B")
    image.save(OUTPUT / filename, dpi=(180, 180))
    with Image.open(OUTPUT / filename) as check:
        check.verify()


def main():
    guide = ["# Capturas de código para el reporte", "",
             "Imágenes generadas directamente de los archivos Java del proyecto. Se conservan el código y los números de línea; las líneas largas se ajustan al ancho de la imagen. No se modificó el código fuente.", ""]
    for filename, title, selections, placement, caption in FIGURES:
        render(filename, title, selections)
        guide.extend([f"## {title}", "", f"Imagen: [{filename}]({filename})", "",
                      placement, "", caption, ""])
    guide.extend(["Para insertarlas en Word: Insertar > Imágenes > Este dispositivo. Conserva la proporción y utiliza el pie de figura indicado. La numeración propuesta puede ajustarse al orden final del documento.", ""])
    (OUTPUT / "LEEME.md").write_text("\n".join(guide), encoding="utf-8")
    with zipfile.ZipFile(OUTPUT / "capturas_battleship.zip", "w", zipfile.ZIP_DEFLATED) as archive:
        for filename, *_ in FIGURES:
            archive.write(OUTPUT / filename, filename)
        archive.write(OUTPUT / "LEEME.md", "LEEME.md")
    for filename, *_ in FIGURES:
        with Image.open(OUTPUT / filename) as rendered:
            print(f"{filename}: {rendered.width} x {rendered.height}")


if __name__ == "__main__":
    main()
