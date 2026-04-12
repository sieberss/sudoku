package org.example;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Sudoku {
    private static final Set<Integer> ALL_NUMBERS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
    private enum IndexType {ROW, COL, BOX};
    private static final Comparator<Field> FEWEST_CANDIDATES 
            = Comparator.comparing(Field::countCandidates)
                        .thenComparing(Field::row)
                        .thenComparing(Field::col);

    private final int[][] board  = new int[9][9];
    // all fields on the board
    private final Field[][] fields = new Field[9][9];
    // still empty fields
    private final List<Field> openFields = new ArrayList<>();
    // those already solved
    private final List<Field> solved =  new ArrayList<>();

    public Sudoku() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                addEmptyField(row, col);
            }
        }
    }

    private void addEmptyField(int row, int col) {
        int box = (row / 3) * 3 +  col / 3;
        Field field = new Field(row, col, box, new HashSet<>(ALL_NUMBERS));
        fields[row][col] = field;
        openFields.add(field);
    }

    private int getBoardEntry(Field f) {
        return board[f.row()][f.col()];
    }

    public Sudoku withGuessedNumberForField(Field guessedField, int guessNumber) {
        Sudoku sudoku = new Sudoku();
        // copy solved entries into new Sudoku object
        solved.forEach(f -> sudoku.markFieldSolvedWithValue(f.row(), f.col(), getBoardEntry(f)));
        // add guess to the new object
        sudoku.markFieldSolvedWithValue(guessedField.row(), guessedField.col(), guessNumber);
        return sudoku;
    }

    private String stringify(){
        return stringify(board);
    }
    private String stringify(int[][] board) {
        // From "Sudoku board validator" Java Solution
        // Displays sudoku board nicely, with separators between the 3x3 subgrids.
        Function<int[], String> stringifyRow = row ->
                String.format("%d%d%d|%d%d%d|%d%d%d",
                        row[0], row[1], row[2],
                        row[3], row[4], row[5],
                        row[6], row[7], row[8]);
        List<String> rows = Arrays.stream(board)
                .map(stringifyRow).toList();
        return String.join("\n", rows.subList(0,3)) +
                "\n---+---+---\n" +
                String.join("\n", rows.subList(3, 6)) +
                "\n---+---+---\n" +
                String.join("\n", rows.subList(6, 9));
    }

    public void markFieldSolvedWithValue(int row, int col, int value) {
        markFieldSolvedWithValue(fields[row][col], value);
    }
    
    private void markFieldSolvedWithValue(Field field, int value) {
        if (value > 0) {
            int oldValue = getBoardEntry(field);
            if (oldValue != 0 && oldValue != value) {
                throw new IllegalArgumentException("Number must not be changed");
            }
            field.setValue(value);
            board[field.row()][field.col()] = value;
            openFields.remove(field);
            solved.add(field);
            openFields.forEach(f -> f.excludeValueFromRowColAndBox(value, field));
        }
    }
    public int[][] solve() {
        System.out.println("Solving Sudoku...");
        System.out.println(stringify());
        boolean noGuessing = true;
        while (!openFields.isEmpty() && noGuessing) {
            openFields.sort(FEWEST_CANDIDATES);
            noGuessing = hasFieldsWithSingleCandidate()
                || hasNumbersWithSingleIndex();
            // other strategies could be tried before guessing
            System.out.println(stringify());
        }
        if (!openFields.isEmpty())
            return getSolutionByGuessing();
        return board;
    }

    private boolean hasNumbersWithSingleIndex() {
        List<Field> openFieldsWithNumber;
        boolean found = false;
        for (int number = 0; number < 9; number++) {
            for (int row = 0; row < 9; row++) {
                openFieldsWithNumber = getOpenFieldsWithNumber(number, IndexType.ROW, row);
                found |= foundSingleFieldForNumber(number, openFieldsWithNumber);
            }
            for (int col = 0; col < 9; col++) {
                openFieldsWithNumber = getOpenFieldsWithNumber(number, IndexType.COL, col);
                found |= foundSingleFieldForNumber(number, openFieldsWithNumber);
            }
            for (int box = 0; box < 9; box++) {
                openFieldsWithNumber = getOpenFieldsWithNumber(number, IndexType.BOX, box);
                found |= foundSingleFieldForNumber(number, openFieldsWithNumber);
            }
        }
        return found;
    }

    private boolean foundSingleFieldForNumber(int number, List<Field> fieldsWithNumber) {
        if (fieldsWithNumber.size() == 1) {
            markFieldSolvedWithValue(fieldsWithNumber.get(0), number);
            return true;
        }
        return false;
    }

    private List<Field> getOpenFieldsWithNumber(int number, IndexType type, int index) {
        Predicate<Field> fieldHasIndex = switch (type){
            case ROW: yield field -> field.row() == index;
            case COL: yield field -> field.col() == index;
            case BOX: yield field -> field.box() == index;
        };
        return openFields.stream()
                .filter(fieldHasIndex)
                .filter(field -> field.hasCandidate(number))
                .toList();
    }

    private int[][] getSolutionByGuessing() {
        Field guessingField = openFields.get(0);
        List<Integer> numbers = guessingField.getCandidates();
        List<Sudoku> guesses = new ArrayList<>();
        List<int[][]> guessedSolutions = new ArrayList<>();
        for (Integer number : numbers) {
            try {
                System.out.printf("Guess %d in row %d and col%d\n", number, guessingField.row(), guessingField.col());
                Sudoku sudoku = withGuessedNumberForField(guessingField, number);
                guesses.add(sudoku);
                guessedSolutions.add(sudoku.solve());
            }
            catch (IllegalArgumentException e) {
                System.out.println("No success with this guess");
            }
        }
        if (guessedSolutions.size() == 1){
            int[][] solution = guessedSolutions.get(0);
            System.out.println(stringify(solution));
            return solution;
        }
        if (guessedSolutions.size() > 1){
            System.out.println("More than one solution found");
            throw new IllegalArgumentException("More than one solution found");
        }
        else {
            System.out.println("No solution found");
            throw new IllegalArgumentException("No solution found");
        }
    }

    private boolean hasFieldsWithSingleCandidate() {
        int fewest = openFields.get(0).countCandidates();
        if  (fewest == 1) {
            updateSolvedFields();
            return true;
        }
        return false;
    }

    private void updateSolvedFields() {
        List<Field> foundSolved = openFields.stream()
            .filter(f -> f.countCandidates() == 1)
            .toList();
        for (Field field : foundSolved) {
            markFieldSolvedWithValue(field, field.getSolution());
            System.out.printf("Solution %d in row %d and col %d\n ", field.getSolution(), field.row(), field.col());
        }
    }

}
