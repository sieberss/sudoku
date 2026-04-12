package org.example;

public class SudokuSolver {

    Sudoku sudoku = new Sudoku();

    public SudokuSolver(int[][] puzzle) {
        if (puzzle.length != 9)
            throw new IllegalArgumentException("Invalid length");
        for (int i = 0; i < 9; i++){
            if (puzzle[i].length != 9)
                throw new IllegalArgumentException("Invalid width");
            for (int j = 0; j < 9; j++){
                if (puzzle[i][j] < 0 || puzzle[i][j] > 9)
                    throw new IllegalArgumentException("Invalid value");
                sudoku.markFieldSolvedWithValue(i, j, puzzle[i][j]);
            }
        }
    }

    public int[][] solve() {
        return sudoku.solve();
    }
}