import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class QuizGame extends JFrame {
    private JLabel questionLabel;
    private JButton[] optionButtons;
    private JLabel scoreLabel;
    private int currentQuestion = 0;
    private int score = 0;

    // Question data
    private final String[][] questions = {
        {"What is the capital of France?", "London", "Paris", "Berlin", "Madrid", "2"},
        {"Which planet is known as the Red Planet?", "Venus", "Jupiter", "Mars", "Saturn", "3"},
        {"What is 2 + 2?", "3", "4", "5", "6", "2"}
    };

    public QuizGame() {
        // Window setup
        setTitle("Quiz Game");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255)); // Light blue background

        // Question label
        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionLabel.setForeground(new Color(25, 25, 112)); // Dark blue text
        add(questionLabel, BorderLayout.NORTH);

        // Panel for options
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        optionsPanel.setBackground(new Color(240, 248, 255));
        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 14));
            optionButtons[i].setBackground(new Color(135, 206, 235)); // Sky blue buttons
            optionButtons[i].setForeground(Color.BLACK);
            optionButtons[i].addActionListener(new OptionListener(i + 1));
            optionsPanel.add(optionButtons[i]);
        }
        add(optionsPanel, BorderLayout.CENTER);

        // Score label
        scoreLabel = new JLabel("Score: 0 / " + questions.length, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        scoreLabel.setForeground(new Color(0, 100, 0)); // Dark green text
        add(scoreLabel, BorderLayout.SOUTH);

        // Load first question
        loadQuestion();
    }

    private void loadQuestion() {
        if (currentQuestion >= questions.length) {
            endQuiz();
            return;
        }

        String[] qData = questions[currentQuestion];
        questionLabel.setText("Q" + (currentQuestion + 1) + ": " + qData[0]);
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(qData[i + 1]);
        }
    }

    private void endQuiz() {
        questionLabel.setText("Quiz Completed!");
        for (JButton button : optionButtons) {
            button.setEnabled(false);
        }
        scoreLabel.setText("Final Score: " + score + " / " + questions.length + " (" + 
            (score * 100 / questions.length) + "%)");
    }

    private class OptionListener implements ActionListener {
        private int selectedOption;

        public OptionListener(int option) {
            this.selectedOption = option;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int correctAnswer = Integer.parseInt(questions[currentQuestion][5]);
            if (selectedOption == correctAnswer) {
                score++;
                JOptionPane.showMessageDialog(QuizGame.this, "Correct!", "Result", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(QuizGame.this, 
                    "Incorrect! Correct answer: " + questions[currentQuestion][correctAnswer], 
                    "Result", JOptionPane.ERROR_MESSAGE);
            }
            scoreLabel.setText("Score: " + score + " / " + questions.length);
            currentQuestion++;
            loadQuestion();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuizGame quiz = new QuizGame();
            quiz.setVisible(true);
        });
    }
}