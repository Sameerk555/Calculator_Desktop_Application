package calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class Calculator extends JFrame implements ActionListener {
    private JTextField display;  // The display text field
    private StringBuilder input; // To hold user input as a string
    private boolean isResultDisplayed; // To track if the result was just displayed
    private boolean isDefaultZero; // To track if the display is showing the default 0

    public Calculator() {
        input = new StringBuilder();
        isResultDisplayed = false;  // Initially, no result has been displayed
        isDefaultZero = true;  // Initially, the display shows "0"
        
        // Create the display field
        display = new JTextField("0");  // Initialize display to "0"
        display.setEditable(false);  // Users cannot directly type into this field
        display.setFont(new Font("Arial", Font.BOLD, 24));
        display.setHorizontalAlignment(JTextField.RIGHT);
        
        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 4, 10, 10));  // Grid layout for buttons
        
        // Array of button labels
        String[] buttonLabels = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            ".", "0", "=", "+",
            "(", ")", "C", "←"
        };
        
        // Create and add buttons to the panel
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            if(label=="+" || label =="-" || label=="/" || label=="*") {
            	button.setForeground(getForeground().MAGENTA);
            }
            else if(label=="C" || label=="←") {
            	button.setForeground(getForeground().RED);
            }
            else if(label=="=") {
            	button.setForeground(getForeground().GREEN);
            }
            else if(label=="(" || label==")") {
            	button.setForeground(getForeground().ORANGE);
            }
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.addActionListener(this);  // Set action listener for each button
            buttonPanel.add(button);
        }
        
        // Set layout of the main frame
        setLayout(new BorderLayout());
        add(display, BorderLayout.NORTH);  // Display at the top
        add(buttonPanel, BorderLayout.CENTER);  // Buttons in the center
        
        // Configure the main frame
        setTitle("Calculator");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (isResultDisplayed) {
            // If an operator is pressed, use the result (whether negative or positive) for the next operation
            if (isOperator(command.charAt(0))) {
                isResultDisplayed = false;  // Reset the result displayed flag
                input.append(command);      // Append the operator to the current result
                display.setText(input.toString());
                return;
            }
            // If a number or decimal point is pressed after the result, clear the input
            else if (Character.isDigit(command.charAt(0)) || command.equals(".")) {
                input.setLength(0);  // Clear the input
                isResultDisplayed = false;  // Reset the result displayed flag

                // Special handling for decimal point: show "0." if decimal is pressed
                if (command.equals(".")) {
                    input.append("0.");  // Show "0." after result is cleared
                    display.setText("0.");
                    isDefaultZero = false;  // Disable default zero flag
                    return;
                }

                // If a number is pressed, just replace the result with the new number
                input.append(command);
                display.setText(input.toString());
                isDefaultZero = false;  // Disable default zero flag
                return;
            }
        }

        switch (command) {
            case "=":
                try {
                    if (input.length() > 0) {
                        // Handle case like "x +" where a number is followed by an operator
                        if (isOperator(input.charAt(input.length() - 1))) {
                            input.append("0");  // Append 0 as the second operand
                        }
                        //handle case if result is negative and you want to perform operation with this 
                        if(isOperator(input.charAt(0))) {
                        	input.insert(0, "0");
                        }
                        double result = evaluate(input.toString());

                        // Format result: if it's an integer, display as integer; otherwise, display with up to 9 decimal places
                        String resultString;
                        if (result == Math.floor(result)) {
                            resultString = String.valueOf((int) result);  // Display as integer
                        } else {
                            resultString = String.format("%.9f", result).replaceAll("0*$", "").replaceAll("\\.$", "");  // Display up to 9 decimal places
                        }
                        display.setText(resultString);

                        input.setLength(0);  // Clear the input
                        input.append(resultString);  // Keep the result for further operations
                        isResultDisplayed = true;  // Mark that the result is displayed
                    } else {
                        display.setText("0");
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    input.setLength(0);  // Clear input on error
                }
                break;
            case "C":
                // Clear the display and input
                input.setLength(0);
                display.setText("0");
                isDefaultZero = true;
                break;
            case "←":
                // Handle backspace (delete the last character)
                if (input.length() > 0) {
                    input.setLength(input.length() - 1);
                    display.setText(input.length() > 0 ? input.toString() : "0");
                    isDefaultZero = input.length() == 0;
                }
                break;
            default:
                // If the default zero is showing and the user presses the decimal point, display "0."
                if (isDefaultZero && command.equals(".")) {
                    input.setLength(0);  // Clear input
                    input.append("0.");  // Display "0."
                    display.setText(input.toString());
                    isDefaultZero = false;
                    break;
                }

                // If the default zero is showing and the user presses a number, clear the display
                if (isDefaultZero && Character.isDigit(command.charAt(0))) {
                    input.setLength(0);  // Clear input
                    isDefaultZero = false;
                }

                // Append input for other buttons (numbers, operators, brackets)
                if (isDefaultZero && isOperator(command.charAt(0))) {
                    input.append("0").append(command);  // Use 0 as the first operand
                    isDefaultZero = false;
                }
               
                else {
                    input.append(command);
                }
                display.setText(input.toString());
                break;
        }
    }
  
    
    // Method to evaluate the arithmetic expression
    public double evaluate(String expression) throws Exception {
        return evaluateExpression(expression);
    }

    // Method to parse and evaluate the expression using two stacks
    private double evaluateExpression(String expression) throws Exception {
        Stack<Double> values = new Stack<>();  // Stack to store numbers
        Stack<Character> operators = new Stack<>();  // Stack to store operators

        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);

            // Skip whitespace
            if (current == ' ') continue;

            // If it's a digit or a decimal point, parse the number
            if (Character.isDigit(current) || current == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && 
                      (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i++));
                }
                i--; // Move back as the loop moves ahead
                values.push(Double.parseDouble(num.toString()));
            }
            // Handle opening bracket '('
            else if (current == '(') {
                operators.push(current);
            }
            // Handle closing bracket ')'
            else if (current == ')') {
                while (operators.peek() != '(') {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop(); // Remove the '(' from the stack
            }
            // Handle operators: +, -, *, /
            else if (isOperator(current)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(current)) {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(current);
            }
        }

        // Apply remaining operations
        while (!operators.isEmpty()) {
            values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop(); // The final value on the stack is the result
    }

    // Method to check if the character is an operator
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    // Method to return precedence of operators
    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    // Method to apply the operation on two operands
    private double applyOperation(char operator, double b, double a) throws Exception {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) throw new Exception("Cannot divide by zero");
                return a / b;
            default:
                throw new Exception("Unknown operator");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator());
    }
}
