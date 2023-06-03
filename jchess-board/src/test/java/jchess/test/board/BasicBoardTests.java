package jchess.test.board;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import jchess.board.Board;
import jchess.board.Moves;
import jchess.board.Pieces;
import jchess.test.lib.Benchmarks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BasicBoardTests {

    // makes all the moves in the given list
    // on the board and prints them out for debugging purposes
    private void makeAndPrintAllMoves(Board board, LongArrayList list) {
        System.out.println(list.size() + " moves | ORIGINAL BOARD: " + board);
        for (int i = 0; i < list.size(); i++) {
            long move = list.getLong(i);

            board.makeMove(move);
            System.out.println("MOVE " + i + ": " + board);
            board.unmakeMove(move);
        }
    }

    @Test
    void test_Pos() {
        Board board = new Board();

        for (int file = 0; file < 8; file++) {
            for (int rank = 1; rank < 8; rank++) {
                int pos = Board.pos(file, rank);
                Assertions.assertEquals(file, Board.file(pos));
                Assertions.assertEquals(rank, Board.rank(pos));
            }
        }
    }

    @Test
    void test_MoveData() {
        long move;

        move = Moves.createWideMove(1, 2);
        move = Moves.setCaptured(move, (byte) (Pieces.WHITE | Pieces.QUEEN));
        move = Moves.setPromotion(move, Moves.PROMOTE_QUEEN);
        move = Moves.makeEnPassant(move);
        Assertions.assertEquals(1, Moves.getSourcePosition(move));
        Assertions.assertEquals(2, Moves.getDestinationPosition(move));
        Assertions.assertEquals((byte)(Pieces.WHITE | Pieces.QUEEN), Moves.getCaptured(move));
        Assertions.assertEquals(Moves.PROMOTE_QUEEN, Moves.getPromotion(move));
        Assertions.assertTrue(Moves.isEnPassant(move));
    }

    @Test
    void test_PromotionMade() {
        // create board
        Board board = new Board();
        board.set(0, 6, Pieces.WHITE | Pieces.PAWN);
        Assertions.assertEquals(Pieces.WHITE | Pieces.PAWN, board.get(0, 6));
        Assertions.assertEquals(Board.WHITE, board.getTurn());

        // make the promotion move
        board.makeMove(Moves.setPromotion(board.createMove(Board.pos(0, 6), Board.pos(0, 7)), Moves.PROMOTE_QUEEN));
        Assertions.assertEquals(Pieces.WHITE | Pieces.QUEEN, board.get(0, 7));
        Assertions.assertEquals(Board.BLACK, board.getTurn());
    }

    @Test
    void test_UnmakeMove_Capture() {
        // create board
        Board board = new Board();
        board.set(1, 1, Pieces.WHITE | Pieces.QUEEN);
        board.set(0, 0, Pieces.BLACK | Pieces.BISHOP);
        Assertions.assertEquals(Pieces.WHITE | Pieces.QUEEN, board.get(1, 1));
        Assertions.assertEquals(Pieces.BLACK | Pieces.BISHOP, board.get(0, 0));

        // make capture
        long captureMove = board.createMove(Board.pos(0, 0), Board.pos(1, 1));
        board.makeMove(captureMove);
        Assertions.assertEquals(Pieces.WHITE | Pieces.QUEEN, Moves.getCaptured(captureMove));
        Assertions.assertEquals(Pieces.BLACK | Pieces.BISHOP, board.get(1, 1));
        Assertions.assertEquals(0, board.get(0, 0));

        // unmake capture
        board.unmakeMove(captureMove);
        Assertions.assertEquals(Pieces.WHITE | Pieces.QUEEN, board.get(1, 1));
        Assertions.assertEquals(Pieces.BLACK | Pieces.BISHOP, board.get(0, 0));
    }

    @Test
    void test_MoveGen_UnverifiedVarious() {
        // create board
        Board board = new Board();

        // generate moves
        LongArrayList moveList = new LongArrayList();
    }

    @Test
    void benchmark_MoveGen_UnverifiedOneQueen() {
        // create board
        Board board = new Board();
        board.set(5, 4, Pieces.WHITE | Pieces.QUEEN);
        LongArrayList moveList = new LongArrayList();
        Benchmarks.performBenchmark("MoveGen_UnverifiedOneQueen", __ -> {
            // generate moves
            moveList.clear();
            board.generateAllUnverifiedMoves(moveList);
        }, 1_000_000_000, 1_000_000_000).print();
    }

}
