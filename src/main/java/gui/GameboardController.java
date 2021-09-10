package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import logic.Dice;

import java.util.HashMap;
import java.util.Map;

public class GameboardController {

    @FXML
    private GridPane guiBoard;

    @FXML
    private Button diceRollButton;

    @FXML
    private Label diceRoll;

    @FXML
    void roll(ActionEvent event) {
        diceRoll.setText(Dice.roll() + "");
    }


    @FXML
    void initialize() {
        //create all pieces
        //set event handlers
        //can access gridpane cells using row and column indices
        //indices start at 1 since row 0 and column 0 are used to display rank and file

        char[][] boardState = parseFENd("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 1");
        for (int i = 1; i < boardState.length; i++) {
            for (int j = 1; j < boardState.length; j++) {
                guiBoard.add(createPiece(boardState[i][j], i, j), j, i);
            }
        }
    }

    public VBox createPiece(char p, int row, int col) {
        Label piece = new Label(p + "");
        piece.setFont(Font.font(42));

        VBox tile = new VBox();
        tile.setAlignment(Pos.CENTER);
        tile.getChildren().add(piece);

        if ((row + col) % 2 == 0) {
            //white cells: row + col % 2 == 0
            tile.setStyle("-fx-background-color: #ffffff");
            piece.setTextFill(Color.BLACK);
        } else {
            //black cells: row + col % 2 == 1
            tile.setStyle("-fx-background-color: #000000");
            piece.setTextFill(Color.WHITE);
        }

        return tile;
    }

    public char[][] parseFENd(String fenDiceBoard) {
        char[][] board = new char[9][9];

        String[] info = fenDiceBoard.split("/|\\s");

        String activeColor = info[8];
        String castling = info[9];
        String enPassant = info[10];
        String halfmoveClock = info[11];
        String fullmoveNumber = info[12];
        int roll = Integer.parseInt(info[13]);

        for (int i = 0; i < 8; i++) {
            char[] rankSequence = info[i].toCharArray();
            char[] rank = board[i+1];
            int index = 1;
            for (char c : rankSequence) {
                if (Character.isDigit(c)) {
                    index += Character.getNumericValue(c);
                } else if (Character.isAlphabetic(c)) {
                    rank[index++] = c;
                }
            }
        }

        return  board;
    }

}
