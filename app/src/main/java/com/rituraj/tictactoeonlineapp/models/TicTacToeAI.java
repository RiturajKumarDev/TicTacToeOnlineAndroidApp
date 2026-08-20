package com.rituraj.tictactoeonlineapp.models;

public class TicTacToeAI {
    public static final char AI = 'O';
    public static final char HUMAN = 'X';
    public static final char EMPTY = '_';

    public static class Move {
        public int row, col;

        public Move(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public Move findBestMove(char[][] board) {

        int bestVal = Integer.MIN_VALUE;
        Move bestMove = new Move(-1, -1);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    board[i][j] = AI;
                    int moveVal = minimax(board, 0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    board[i][j] = EMPTY;

                    if (moveVal > bestVal) {
                        bestMove = new Move(i, j);
                        bestVal = moveVal;
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(char[][] board, int depth, boolean isMax, int alpha, int beta) {
        int score = evaluate(board);
        if (score == 10)
            return score - depth;
        if (score == -10)
            return score + depth;
        if (!isMovesLeft(board))
            return 0;
        if (isMax) {
            int best = Integer.MIN_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = AI;
                        best = Math.max(best, minimax(board, depth + 1, false, alpha, beta));
                        board[i][j] = EMPTY;
                        alpha = Math.max(alpha, best);
                        if (beta <= alpha)
                            break;
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = HUMAN;
                        best = Math.min(best, minimax(board, depth + 1, true, alpha, beta));
                        board[i][j] = EMPTY;
                        beta = Math.min(beta, best);
                        if (beta <= alpha)
                            break;
                    }
                }
            }
            return best;
        }
    }

    private boolean isMovesLeft(char[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY)
                    return true;
            }
        }
        return false;
    }

    private int evaluate(char[][] board) {
        // Rows
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2]) {
                if (board[row][0] == AI)
                    return 10;
                else if (board[row][0] == HUMAN)
                    return -10;
            }
        }

        // Columns
        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                if (board[0][col] == AI)
                    return 10;
                else if (board[0][col] == HUMAN)
                    return -10;
            }
        }

        // Diagonals
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == AI)
                return 10;
            else if (board[0][0] == HUMAN)
                return -10;
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == AI)
                return 10;
            else if (board[0][2] == HUMAN)
                return -10;
        }
        return 0;
    }
}
