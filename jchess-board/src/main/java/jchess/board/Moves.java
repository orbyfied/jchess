package jchess.board;

import java.util.BitSet;

/**
 * A simple move is represented by a 16 bit integer (short).
 * The leading 8 bits represent the source position, and the
 * last 8 bits represent the destination move.
 *
 * Moves can be analyzed and the data can be cached for performance.
 * These are called wide moves, they are represented by a 64 bit integer (long).
 * The leading 16 bits (short) is the same format as a simple move, the last
 * 48 bits are reserved for extra analyzed data.
 *
 * The layout of the last 48 bits is as follows:
 * {@code pPPccccccce}
 * Where:
 * p = Is promotion?
 * P = Promotion piece (0 = Knight, 1 = Bishop, 2 = Rook, 3 = Queen)
 *   These two compound into the 3-bit promotion field as {@code pPP}
 * c = The captured piece
 * e = Whether the move is an en passant.
 */
public final class Moves {

    /* Promotion Field Values */
    public static final byte PROMOTE_NONE   = 0b000;
    public static final byte PROMOTE_KNIGHT = 0b100;
    public static final byte PROMOTE_BISHOP = 0b101;
    public static final byte PROMOTE_ROOK   = 0b110;
    public static final byte PROMOTE_QUEEN  = 0b111;

    /**
     * Create a simple move going from the source to the destination position.
     */
    public static short createSimpleMove(byte src, byte dst) {
        return (short) ((short)(src << 8) | dst >> 8);
    }

    /**
     * Create a simple move going from the source to the destination position.
     */
    public static short createSimpleMove(int src, int dst) {
        return createSimpleMove((byte) src, (byte) dst);
    }

    /**
     * Create a wide move going from the source to the destination position.
     */
    public static long createWideMove(byte src, byte dst) {
        return ((long) src << 56) | ((long) dst << 48);
    }

    /**
     * Create a wide move going from the source to the destination position.
     */
    public static long createWideMove(int src, int dst) {
        return createWideMove((byte) src, (byte) dst);
    }

    public static byte getSourcePosition(short move) {
        return (byte) (move & 0xFF);
    }

    public static byte getSourcePosition(long move) {
        return (byte) ((move >> 56) & 0xFF);
    }

    public static byte getDestinationPosition(short move) {
        return (byte) (move & 0x00FF);
    }

    public static byte getDestinationPosition(long move) {
        return (byte) ((move >> 48) & 0x00FF);
    }

    /**
     * Convert the given simple move into a wide move.
     */
    public static long toWide(short move) {
        return (long) move << 48;
    }

    /**
     * Set the promotion field on the given move.
     */
    public static long setPromotion(short move, byte field) {
        return setPromotion(toWide(move), field);
    }

    /**
     * Set the promotion field on the given move.
     */
    public static long setPromotion(long move, byte field) {
        return move | ((long) (field & 0b111) << 45);
    }

    /**
     * Get the promotion field on the given move.
     */
    public static byte getPromotion(long move) {
        return (byte) ((move >> 45) & 0b111);
    }

    /**
     * Set the captured piece field in the given move.
     *
     * @param move The move.
     * @param piece The piece.
     * @return The new move.
     */
    public static long setCaptured(long move, byte piece) {
        return move | ((long) (int) piece << 38);
    }

    /**
     * Get the captured piece field from the given move.
     *
     * @param move The move.
     * @return The captured field.
     */
    public static byte getCaptured(long move) {
        return (byte) ((move >> 38) & 0b1111111);
    }

    /**
     * Enable the en passant bit on the given move.
     *
     * @param move The move.
     * @return The en passant bit.
     */
    public static long makeEnPassant(long move) {
        return move | (1L << 37);
    }

    /**
     * Check whether the given move in en passant.
     *
     * @param move The move.
     * @return Whether it is.
     */
    public static boolean isEnPassant(long move) {
        return ((move >> 37) & 0x1) != 0;
    }

    /**
     * Get the new piece that should be placed at the destination square.
     */
    public static byte getPromotionPiece(long move, byte currentPiece) {
        int col = Pieces.getColor(currentPiece);
        byte pf = getPromotion(move);
        return (byte) switch (pf) {
            case PROMOTE_KNIGHT -> Pieces.KNIGHT | col;
            case PROMOTE_BISHOP -> Pieces.BISHOP | col;
            case PROMOTE_ROOK   -> Pieces.ROOK   | col;
            case PROMOTE_QUEEN  -> Pieces.QUEEN  | col;

            default -> currentPiece;
        };
    }

    private static final String BIT_REPR_ANNOTATION = "Src.....Dst.....PrmCap....E";

    /**
     * Get a debug string representing the given move.
     *
     * @param move The move.
     * @return The string.
     */
    public static String debugString(long move) {
        BitSet bitSet = BitSet.valueOf(new long[] { move });
        StringBuilder b = new StringBuilder();
        b.append("| MOVE: ").append(bitSet.length()).append(" sigBits\n");

        // bit repr
        int len = bitSet.length();
        b.append("| ").append(BIT_REPR_ANNOTATION).append("\n");
        b.append("| ").append("0".repeat(64 - len)).append(Long.toBinaryString(bitSet.toLongArray()[0])).append("\n");

        // info
        b.append("| ").append("src: ").append(getSourcePosition(move)).append(", dst: ").append(getDestinationPosition(move)).append("\n");

        return b.toString();
    }

    public static void debugPrint(String label, long move) {
        System.out.println(label + ": \n" + debugString(move));
    }

}
