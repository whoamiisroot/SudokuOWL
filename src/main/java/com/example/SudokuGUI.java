package com.example;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class SudokuGUI extends JFrame {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private Color userInputColor = Color.RED;
    private Color solverInputColor = Color.WHITE;

    public SudokuGUI() {
        setTitle("OWL Sudoku solver");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE)) {
            @Override
            public Insets getInsets() {
                return new Insets(10, 10, 10, 10);
            }
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                cells[row][col].setBackground(Color.WHITE); // Set background color to white
                cells[row][col].setBorder(BorderFactory.createMatteBorder(
                        row % SUBGRID_SIZE == 0 ? 2 : 1, // thicker border for rows divisible by 3
                        col % SUBGRID_SIZE == 0 ? 2 : 1, // thicker border for columns divisible by 3
                        1,
                        1,
                        Color.BLACK)); // Add black border

                // Add document filter to restrict input to numbers 1-9
                ((AbstractDocument) cells[row][col].getDocument()).setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                            throws BadLocationException {
                        String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                        if (newText.matches("[1-9]{0,1}")) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }
                });

                cells[row][col].addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        JTextField source = (JTextField) evt.getSource();
                        if (!source.getText().isEmpty()) {
                            source.setBackground(Color.WHITE);
                        }
                    }
                });
                gridPanel.add(cells[row][col]);
            }
        }

        JButton solveButton = new JButton("Solve");
        solveButton.setBackground(Color.GREEN); // Set solve button color to green
        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                solveSudoku();
            }
        });

        JButton resetButton = new JButton("Reset");
        resetButton.setBackground(Color.RED); // Set reset button color to red
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGrid();
            }
        });

        // resetButton.setEnabled(false); // Remove this line to enable the "Reset" button

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(solveButton);
        buttonPanel.add(resetButton);

        add(gridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void solveSudoku() {
        int[][] sudoku = new int[SIZE][SIZE];

        // Read values from the user interface grid
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = cells[row][col].getText();
                if (!text.isEmpty()) {
                    sudoku[row][col] = Integer.parseInt(text);
                } else {
                    sudoku[row][col] = 0;
                }
            }
        }

        // Check if the grid is valid before solving
        if (!isValidSudoku(sudoku)) {
            JOptionPane.showMessageDialog(this, "The grid is invalid. Please check your input.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Use solving logic
        SolverOWL solver = new SolverOWL();
        if (solver.solve(sudoku)) {
            // Display results in the graphical interface
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (cells[row][col].getBackground() != userInputColor) {
                        cells[row][col].setText(String.valueOf(sudoku[row][col]));
                        cells[row][col].setBackground(solverInputColor);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Cannot solve Sudoku with this configuration.", "Error", JOptionPane.ERROR_MESSAGE);
            resetGrid(); // Reset the grid in case solving is not possible
        }
    }

    private void resetGrid() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col].setText("");
                cells[row][col].setBackground(Color.WHITE); // Set background color to white
            }
        }
    }

    private boolean isValidSudoku(int[][] board) {
        // Check rows and columns
        for (int i = 0; i < SIZE; i++) {
            Set<Integer> rowSet = new HashSet<>();
            Set<Integer> colSet = new HashSet<>();
            for (int j = 0; j < SIZE; j++) {
                if (rowSet.contains(board[i][j]) && board[i][j] != 0) {
                    return false; // Duplicate number in the same row
                }
                if (colSet.contains(board[j][i]) && board[j][i] != 0) {
                    return false; // Duplicate number in the same column
                }
                rowSet.add(board[i][j]);
                colSet.add(board[j][i]);
            }
        }

        // Check 3x3 subgrids
        for (int i = 0; i < SIZE; i += SUBGRID_SIZE) {
            for (int j = 0; j < SIZE; j += SUBGRID_SIZE) {
                Set<Integer> subgridSet = new HashSet<>();
                for (int k = i; k < i + SUBGRID_SIZE; k++) {
                    for (int l = j; l < j + SUBGRID_SIZE; l++) {
                        if (subgridSet.contains(board[k][l]) && board[k][l] != 0) {
                            return false; // Duplicate number in the same subgrid
                        }
                        subgridSet.add(board[k][l]);
                    }
                }
            }
        }

        return true; // The grid is valid
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SudokuGUI().setVisible(true);
            }
        });
    }
}
