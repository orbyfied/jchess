package jchess.board;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.Arrays;

/**
 * The chess board is fundamentally represented by a 1-dimensional array
 * of 64 bytes. Each byte represents a square following the format defined
 * in {@link Pieces}.
 */
public class Board {

    /**
     * Pack a reference to the square at the given position
     * with the given piece on it into a 16-bit integer (short).
     *
     * @param pos The position.
     * @param piece The piece.
     * @return The packed data.
     */
    public static short packSquare(int pos, byte piece) {
        byte bPos = (byte) pos;
        return (short) (((short) piece) | bPos);
    }

    /**
     * Get the position from the given packed square value.
     */
    public static int getPosition(short sqr) {
        return sqr & 0xFF;
    }

    /**
     * Get the piece value from the given packed square value.
     */
    public static byte getPiece(short sqr) {
        return (byte) ((sqr << 8) & 0xFF);
    }

    public static final int WHITE = 1;
    public static final int BLACK = 0;

    public static String getColorName(int col) {
        return col == WHITE ? "white" : "black";
    }

    /**
     * The basic board array.
     */
    private final byte[] board = new byte[64];

    /**
     * The current turn on the board.
     */
    private int turn = 1;

    /**
     * What white pawn can be victim to en passant.
     */
    private int canBeEnPassantVictimWhite = -1;

    /**
     * What black pawn can be victim to en passant.
     */
    private int canBeEnPassantVictimBlack = -1;

    /**
     * The list of all positions of all pieces. This makes
     * move generation significantly faster.
     */
    private final IntArrayList pieces = new IntArrayList();

    /**
     * Get the current turn.
     *
     * @return The turn.
     */
    public int getTurn() {
        return turn;
    }

    /**
     * Set the current turn.
     *
     * @param turn The turn.
     */
    public void setTurn(byte turn) {
        this.turn = turn;
    }

    /**
     * Get the rank number of the position from top (0) to bottom (7)
     * inclusive.
     *
     * @param pos The 1D position.
     * @return The rank.
     */
    public static int rank(int pos) {
        return (pos & 0b111000) >> 3;
    }

    /**
     * Get the file number of the position from left (0) to right (7)
     * inclusive.
     *
     * @param pos The 1D position.
     * @return The file.
     */
    public static int file(int pos) {
        return pos & 0b000111;
    }

    /**
     * Get the 1D position for the given file and rank.
     *
     * @param file The file.
     * @param rank The rank.
     * @return The 1D position.
     */
    public static int pos(int file, int rank) {
        return rank * 8 + file;
    }

    /**
     * Get the content of the square at the given position.
     *
     * @param pos The position.
     * @return The piece.
     */
    public byte get(int pos) {
        return board[pos];
    }

    public byte get(int file, int rank) {
        return board[rank * 8 + file];
    }

    /**
     * Set the value of the square at the given position to
     * the given piece/value.
     *
     * @param pos The position.
     * @param piece The value.
     */
    public void set(int pos, byte piece) {
        board[pos] = piece;

        // update lists
        if (piece == 0) pieces.rem(pos);
        else pieces.add(pos);
    }

    /**
     * Set the value of the square at the given position to
     * the given piece/value.
     *
     * @param pos The position.
     * @param piece The value.
     */
    public void set(int pos, int piece) {
        board[pos] = (byte) piece;
    }

    public void set(int file, int rank, byte piece) {
        set(rank * 8 + file, piece);
    }

    public void set(int file, int rank, int piece) {
        set(rank * 8 + file, (byte) piece);
    }

    /**
     * Check whether the board has a piece at the given position.
     *
     * @param pos The position.
     * @return Whether a piece exists.
     */
    public boolean hasPiece(int pos) {
        return board[pos] != 0;
    }

    /**
     * Check whether the piece at the given position is of the given color.
     *
     * @param pos The position.
     * @param col The color.
     * @return Whether the piece exists and is of the given color.
     */
    public boolean isPieceOfColor(int pos, byte col) {
        byte piece = board[pos];
        return piece != 0 && (byte) (piece & 0b0001000) == col;
    }

    /**
     * Create an analyzed move from the source to the destination.
     * 
     * @param src The source.
     * @param dst The destination.
     * @return The move.
     */
    public long createMove(int src, int dst) {
        long move = Moves.createWideMove(src, dst);
        
        // store capture
        byte captured = board[dst];
        if (captured != 0)
            move = Moves.setCaptured(move, captured);
        
        return move;
    }

    /**
     * Make the given move on the board.
     *
     * @param move The move.
     */
    public void makeMove(long move) {
        // extract positions
        int src = Moves.getSourcePosition(move);
        int dst = Moves.getDestinationPosition(move);

        // make move
        byte piece = board[src];
        set(src, 0);
        if ((piece & 0b1110000) == Pieces.PAWN) // check for promotion
            piece = Moves.getPromotionPiece(move, piece);
        if (Moves.isEnPassant(piece)) { // check for en passant
            set(dst, 0);
            int col = Pieces.getColor(piece);

            if (col == Pieces.WHITE) set(dst + 8, piece);
            else set(dst - 8, piece);
        } else set(dst, piece);

        // reset en passant values
        if (turn == WHITE)
            canBeEnPassantVictimBlack = -1;
        else
            canBeEnPassantVictimWhite = -1;

        // switch the turn
        turn = turn == WHITE ? BLACK : WHITE;
    }

    /**
     * Unmake the given move from the board.
     *
     * @param move The move.
     */
    public void unmakeMove(long move) {
        // TODO: reset various states like en passant map

        // extract positions
        int src = Moves.getSourcePosition(move);
        int dst = Moves.getDestinationPosition(move);

        // unmake move
        byte dPiece = board[dst];
        int col = Pieces.getColor(dPiece);
        
        byte pp = Moves.getPromotionPiece(move, (byte) 0xFF);
        if (pp != (byte)0xFF) {
            // undo promotion
            board[dst] = 0;
            board[src] = (byte) (Pieces.PAWN | col);
            return;
        } else {
            // undo simple move
            // todo: support en passant undo
            byte piece = board[dst];
            board[dst] = Moves.getCaptured(move);
            board[src] = piece;

            System.out.println("src(og): " + Pieces.toString(piece) + ", dst(captured): " + Pieces.toString(board[dst]));
        }
        
        // switch the turn
        turn = turn == WHITE ? BLACK : WHITE;
    }

    /*
        Move Generation
     */

    /**
     * Generate all pseudo-legal moves in a parallel (not diagonal) line/ray
     * in all directions.
     *
     * @param list The output list.
     * @param pos The starting position.
     * @param col Our color.
     */
    public void generateUnverifiedLineMovesParallel(LongArrayList list, int pos, byte col) {
        int cPos;
        byte piece;

        int sf = file(pos), file;
        int sr = rank(pos), rank;

        // file +
        cPos = pos;
        file = sf;
        while (++file != 8 && !isPieceOfColor(piece = board[cPos += 1], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }

        // file -
        cPos = pos;
        file = sf;
        while (--file != -1 && !isPieceOfColor(piece = board[cPos -= 1], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }

        // rank +
        cPos = pos;
        rank = sr;
        while (++rank != 8 && !isPieceOfColor(piece = board[cPos += 8], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }

        // rank -
        cPos = pos;
        rank = sr;
        while (--rank != -1 && !isPieceOfColor(piece = board[cPos -= 8], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }
    }

    /**
     * Generate all pseudo-legal moves in a diagonal line/ray in all directions.
     *
     * @param list The output list.
     * @param pos The starting position.
     * @param col Our color.
     */
    public void generateUnverifiedLineMovesDiagonal(LongArrayList list, int pos, byte col) {
        int cPos;
        byte piece;

        int sf = file(pos), file;
        int sr = rank(pos), rank;

        // f+ r+
        cPos = pos;
        file = sf;
        rank = sr;
        while (++file != 8 && ++rank != 8 && !isPieceOfColor(piece = board[cPos += 9], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }

        // f- r-
        cPos = pos;
        file = sf;
        rank = sr;
        while (--file != -1 && --rank != 0 && !isPieceOfColor(piece = board[cPos -= 9], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }

        // f+ r-
        cPos = pos;
        file = sf;
        rank = sr;
        while (++file != 8 && --rank != 0 && !isPieceOfColor(piece = board[cPos -= 7], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }

        // f- r+
        cPos = pos;
        file = sf;
        rank = sr;
        while (--file != -1 && ++rank != 8 && !isPieceOfColor(piece = board[cPos += 7], col)) {
            list.add(createMove(pos, cPos));
            if (piece != 0) break;
        }
    }

    /**
     * Generate all pseudo-legal moves for the given piece on the board.
     *
     * @param list The list of moves.
     * @param pos The position of the piece.
     * @param piece The piece.
     */
    public void generateUnverifiedMoves(LongArrayList list, int pos, byte piece) {
        byte type = Pieces.getType(piece);
        byte col = Pieces.getColor(piece);
        byte oppCol = (byte) (col == 0b0000 ? 0b0001 : 0b0000);

        switch (type) {
            case Pieces.PAWN -> {
                int dPos;
                int y = rank(pos);
                int x = file(pos);

                // check the color to determine the
                // directions the pawn can move
                int cs = col == Pieces.WHITE ? 1 : -1; // color sign
                int rs = 8 * cs; // rank sign

                // add one square move to the list
                dPos = pos + rs;
                if (!hasPiece(dPos))
                    list.add(createMove(pos, dPos));

                // check if it can move two squares
                if (col == Pieces.WHITE ? y == 1 : y == 6) {
                    dPos += rs; // dPos already is one rank forward
                    if (!hasPiece(dPos))
                        list.add(createMove(pos, dPos));
                }

                // check for captures
                if (x != 0)
                    if (isPieceOfColor(dPos = pos + rs - 1, oppCol))
                        list.add(createMove(pos, dPos));
                if (x != 7)
                    if (isPieceOfColor(dPos = pos + rs + 1, oppCol))
                        list.add(createMove(pos, dPos));

                // check for en passant
                int victim = col == WHITE ? canBeEnPassantVictimBlack : canBeEnPassantVictimWhite;
                if (victim == pos + 1 || victim == pos - 1)
                    list.add(Moves.makeEnPassant(createMove(pos, victim)));
            }

            case Pieces.KNIGHT -> {
                int dPos;
                int y = rank(pos);
                int x = file(pos);

                if (x != 0 && y > 1) if (!isPieceOfColor(dPos = pos - 16 - 1, col)) list.add(createMove(pos, dPos)); // -1 -2
                if (x > 1 && y != 0) if (!isPieceOfColor(dPos = pos - 8  - 2, col)) list.add(createMove(pos, dPos)); // -2 -1
                if (x != 7 && y < 6) if (!isPieceOfColor(dPos = pos + 16 + 1, col)) list.add(createMove(pos, dPos)); //  1  2
                if (x < 6 && y != 7) if (!isPieceOfColor(dPos = pos + 8  + 2, col)) list.add(createMove(pos, dPos)); //  2  1
                if (x > 1 && y != 7) if (!isPieceOfColor(dPos = pos - 16 + 1, col)) list.add(createMove(pos, dPos)); // -2  1
                if (x != 0 && y < 6) if (!isPieceOfColor(dPos = pos - 8  + 2, col)) list.add(createMove(pos, dPos)); // -1  2
                if (x < 6 && y != 0) if (!isPieceOfColor(dPos = pos + 16 - 1, col)) list.add(createMove(pos, dPos)); //  2 -1
                if (x != 7 && y > 1) if (!isPieceOfColor(dPos = pos +  8 - 2, col)) list.add(createMove(pos, dPos)); //  1 -2
            }

            case Pieces.KING -> {
                int dPos;
                int y = rank(pos);
                int x = file(pos);

                if (x != 7 && y != 7) if (!isPieceOfColor(dPos = pos + 8 + 1, col)) list.add(createMove(pos, dPos));
                if (x != 7 && y != 0) if (!isPieceOfColor(dPos = pos + 8 - 1, col)) list.add(createMove(pos, dPos));
                if (x != 0 && y != 7) if (!isPieceOfColor(dPos = pos - 8 + 1, col)) list.add(createMove(pos, dPos));
                if (x != 0 && y != 0) if (!isPieceOfColor(dPos = pos - 8 - 1, col)) list.add(createMove(pos, dPos));

                if (x != 0) if (!isPieceOfColor(dPos = pos - 1, col)) list.add(createMove(pos, dPos));
                if (x != 7) if (!isPieceOfColor(dPos = pos + 1, col)) list.add(createMove(pos, dPos));
                if (y != 0) if (!isPieceOfColor(dPos = pos - 8, col)) list.add(createMove(pos, dPos));
                if (y != 7) if (!isPieceOfColor(dPos = pos + 8, col)) list.add(createMove(pos, dPos));
            }

            case Pieces.ROOK -> generateUnverifiedLineMovesParallel(list, pos, piece);

            case Pieces.BISHOP -> generateUnverifiedLineMovesDiagonal(list, pos, piece);

            case Pieces.QUEEN -> {
                generateUnverifiedLineMovesParallel(list, pos, piece);
                generateUnverifiedLineMovesDiagonal(list, pos, piece);
            }

            default -> { }
        }
    }

    /**
     * Generate all pseudo-legal moves for all pieces on the board.
     *
     * @param list The output move list.
     */
    public void generateAllUnverifiedMoves(LongArrayList list) {
        final int l = pieces.size();
        for (int i = 0; i < l; i++) {
            int pos = pieces.getInt(i);

            generateUnverifiedMoves(list, pos, board[pos]);
        }
    }

    /**
     * Hash the position into a 64-bit hash code.
     *
     * @return The hash code.
     */
    public long hashPosition() {
        // TODO
        return 0;
    }

    /**
     * Fork the board into a new instance. This is basically
     * just cloning the instance.
     *
     * @return The clone.
     */
    public Board fork() {
        Board board = new Board();

        System.arraycopy(this.board, 0, board.board, 0, 64);
        board.pieces.addAll(this.pieces);
        board.turn = this.turn;
        board.canBeEnPassantVictimBlack = canBeEnPassantVictimBlack;
        board.canBeEnPassantVictimWhite = canBeEnPassantVictimWhite;

        return board;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        // append properties
        b.append("Board(8x8)").append(" turn: ").append(getColorName(turn));
        b.append("\n");

        // append squares
        for (int rank = 7; rank >= 0; rank--) {
            b.append("[");
            for (int file = 0; file < 8; file++) {
                if (file != 0)
                    b.append(", ");

                // append representation
                byte piece = board[rank * 8 + file];
                b.append(Pieces.toCompactString(piece));
            }

            b.append("] ").append(rank + 1).append("\n");
        }

        b.append(" A  B  C  D  E  F  G  H ");

        return b.toString();
    }

}
