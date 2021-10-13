package logic.enums;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public enum Piece {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING,
    EMPTY, OFF_BOARD,

    WHITE_PAWN(PAWN, Side.WHITE, 'P'),
    WHITE_KNIGHT(KNIGHT, Side.WHITE, 'N'),
    WHITE_BISHOP(BISHOP, Side.WHITE, 'B'),
    WHITE_ROOK(ROOK, Side.WHITE, 'R'),
    WHITE_QUEEN(QUEEN, Side.WHITE, 'Q'),
    WHITE_KING(KING, Side.WHITE, 'K'),

    BLACK_PAWN(PAWN, Side.BLACK, 'p'),
    BLACK_KNIGHT(KNIGHT, Side.BLACK, 'n'),
    BLACK_BISHOP(BISHOP, Side.BLACK, 'b'),
    BLACK_ROOK(ROOK, Side.BLACK, 'r'),
    BLACK_QUEEN(QUEEN, Side.BLACK, 'q'),
    BLACK_KING(KING, Side.BLACK, 'k');

    static Map<Character, Piece> charPieceMap = new HashMap<>();
    static EnumMap<Piece, Character> unicodeMap = new EnumMap<>(Piece.class);
    private static final char[] diceToPiece = {'p', 'n', 'b', 'r', 'q', 'k'};
    static {
        charPieceMap.put('P', WHITE_PAWN);
        charPieceMap.put('N', WHITE_KNIGHT);
        charPieceMap.put('B', WHITE_BISHOP);
        charPieceMap.put('R', WHITE_ROOK);
        charPieceMap.put('Q', WHITE_QUEEN);
        charPieceMap.put('K', WHITE_KING);
        charPieceMap.put('p', BLACK_PAWN);
        charPieceMap.put('n', BLACK_KNIGHT);
        charPieceMap.put('b', BLACK_BISHOP);
        charPieceMap.put('r', BLACK_ROOK);
        charPieceMap.put('q', BLACK_QUEEN);
        charPieceMap.put('k', BLACK_KING);
        charPieceMap.put('o', OFF_BOARD);
        charPieceMap.put('\u0000', EMPTY);

        unicodeMap.put(WHITE_PAWN, '♟');
        unicodeMap.put(WHITE_KNIGHT, '♞');
        unicodeMap.put(WHITE_BISHOP, '♝');
        unicodeMap.put(WHITE_ROOK, '♜');
        unicodeMap.put(WHITE_QUEEN, '♛');
        unicodeMap.put(WHITE_KING, '♚');

        unicodeMap.put(BLACK_PAWN, '♙');
        unicodeMap.put(BLACK_KNIGHT, '♘');
        unicodeMap.put(BLACK_BISHOP, '♗');
        unicodeMap.put(BLACK_ROOK, '♖');
        unicodeMap.put(BLACK_QUEEN, '♕');
        unicodeMap.put(BLACK_KING, '♔');

        unicodeMap.put(EMPTY, '　');
        unicodeMap.put(OFF_BOARD, 'o');

    }

    final Piece type;
    final Side color;
    public char charType;

    Piece(Piece type, Side color, char charType) {
        this.type = type;
        this.color = color;
        this.charType = charType;

    }

    Piece() {
        type = this;
        color = Side.NEUTRAL;
    }

    public static Piece getPieceFromChar(char c) {
        return charPieceMap.get(c);
    }
    public char getCharType() {return this.charType;}

    //honestly can probably change it to just return type automatically, but not sure if above constructor is used
    public Piece getType() {
        return switch (this) {
            case PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING, EMPTY, OFF_BOARD -> this;
            default -> type;
        };
    }

    public int[] getOffsets() {
        return switch (this) {
            case WHITE_PAWN -> new int[]{16, 15, 17};
            case BLACK_PAWN -> new int[]{-16, -15, -17};
            //etc
            default -> new int[0];
        };
    }

    public char getUnicode() {
        return unicodeMap.get(this);
    }

    public Side getColor() {
        return color;
    }

    public boolean isFriendly(Side side) {
        return color == side;
    }

    /**
     * Only valid for Pawn pieces
     *
     * @param square at which piece is located
     * @return true if pawn is at the appropriate rank, false in any other case
     */
    public boolean canDoubleJump(Square square) {
        return switch (this) {
            case WHITE_PAWN -> square.getRank() == 2;
            case BLACK_PAWN -> square.getRank() == 7;
            default -> false;
        };
    }

    /**
     * Only valid for Pawn pieces
     *
     * @param square at which piece is located
     * @return true if pawn is at the appropriate rank, false in any other case
     */
    public boolean canPromote(Square square) {
        return switch (this) {
            case WHITE_PAWN -> square.getRank() == 8;
            case BLACK_PAWN -> square.getRank() == 1;
            default -> false;
        };
    }

    public Piece promote(int diceRoll) {
        return switch (this) {
            case WHITE_PAWN -> getPieceFromChar(Character.toUpperCase(diceToPiece[diceRoll - 1]));
            case BLACK_PAWN -> getPieceFromChar(diceToPiece[diceRoll - 1]);
            default -> this;
        };
    }
}