package jchess.board;

/**
 * Pieces are represented by bytes. The upper 3 bits of the byte represent the type,
 * then the 4th bit represents the color (0 = black, 1 = white) and the remaining bits
 * are free to be used as flags.
 *
 * The piece types and their properties are defined as follows:
 * | Type   | dec. ID | bin. ID | fValue
 * -------------------------------------
 * | None   | 0       | 000     | 0
 * | Pawn   | 1       | 100     | 1
 * | Knight | 2       | 010     | 3
 * | Bishop | 3       | 110     | 3.1
 * | Rook   | 4       | 001     | 5
 * | Queen  | 5       | 101     | 9
 * | King   | 6       | 011     | (1)
 * | -      | 7       | 111     (UNUSED)
 *
 * (1): The king is not counted in the material evaluation,
 *      but is considered the most important piece by the search functions.
 */
public final class Pieces {

    /* Types                      TYPE -|  |- COLOR BIT */
    public static final byte NONE   = 0b0000_000;
    public static final byte PAWN   = 0b1000_000;
    public static final byte KNIGHT = 0b0100_000;
    public static final byte BISHOP = 0b1100_000;
    public static final byte ROOK   = 0b0010_000;
    public static final byte QUEEN  = 0b1010_000;
    public static final byte KING   = 0b0110_000;

    /* Color                     TYPE -|  |- COLOR BIT*/
    public static final byte BLACK = 0b0000_000;
    public static final byte WHITE = 0b0001_000;

    /**
     * Create a piece of the specified type and color.
     */
    public static byte createPiece(byte type, byte color) {
        return (byte) (type | color);
    }

    /**
     * Get the color ID of the given piece byte.
     */
    public static byte getColor(byte piece) {
        return (byte) (piece & 0b0001000);
    }

    /**
     * Get the type ID of the given piece byte.
     */
    public static byte getType(byte piece) {
        return (byte) (piece & 0b1110000);
    }

    /**
     * Get the name of the given type ID.
     */
    public static String getTypeName(byte type) {
        return switch (type) {
            case NONE   -> "none";
            case PAWN   -> "pawn";
            case KNIGHT -> "knight";
            case BISHOP -> "bishop";
            case ROOK   -> "rook";
            case QUEEN  -> "queen";
            case KING   -> "king";

            default -> "unknownT(" + type + ")";
        };
    }

    /**
     * Get the name of the given color ID.
     */
    public static String getColorName(byte color) {
        return switch (color) {
            case BLACK -> "black";
            case WHITE -> "white";

            default -> "unknownC(" + color + ")";
        };
    }

    /**
     * Get a string representation of the given piece byte.
     */
    public static String toString(byte piece) {
        if (piece == 0)
            return "none";

        return getTypeName(getType(piece)) + ":" + getColorName(getColor(piece));
    }

    /**
     * Get a compact string representation of the given piece byte.
     */
    public static String toCompactString(byte piece) {
        if (piece == 0)
            return "-";

        char c = switch (getType(piece)) {
            case PAWN   -> 'p';
            case KNIGHT -> 'n';
            case BISHOP -> 'b';
            case ROOK   -> 'r';
            case QUEEN  -> 'q';
            case KING   -> 'k';

            default -> '?';
        };

        if (getColor(piece) == WHITE)
            c = Character.toUpperCase(c);

        return String.valueOf(c);
    }

}
