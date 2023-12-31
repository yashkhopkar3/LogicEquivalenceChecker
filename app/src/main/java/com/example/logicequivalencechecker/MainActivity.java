package com.example.logicequivalencechecker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private EditText expr1EditText;
    private EditText expr2EditText;
    private Button evaluateButton;
    private WebView resultWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expr1EditText = findViewById(R.id.expr1EditText);
        expr2EditText = findViewById(R.id.expr2EditText);
        evaluateButton = findViewById(R.id.evaluateButton);
        resultWebView = findViewById(R.id.resultWebView);
        resultWebView.setBackgroundColor(Color.parseColor("#FAC5B0"));
        TextView textView = findViewById(R.id.inputDescriptionTextView); // Replace with your TextView's ID
        String textWithAmpersand = "Allowed Input: p, q, t, f, (,), &, V, ~, ->, ↔";
        textView.setText(Html.fromHtml(textWithAmpersand));

        evaluateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String expr1 = expr1EditText.getText().toString();
                String expr2 = expr2EditText.getText().toString();
                expr1 = expr1.replaceAll("\\s", ""); // Remove all spaces
                expr2 = expr2.replaceAll("\\s", ""); // Remove all spaces
                String result = evaluateExpressions(expr1, expr2);
                resultWebView.setBackgroundColor(Color.parseColor("#FAC5B0"));
                resultWebView.loadData(result, "text/html", "UTF-8");

            }
        });
    }

    private String evaluateExpressions(String expr1, String expr2) {
        if (!isValidExpression(expr1) || !isValidExpression(expr2)) {
            return "<html><body><p>Invalid expression(s)</p></body></html>";
        }

        // Declare and initialize the truth values for operands P and Q
        boolean[] p = {true, true, false, false};
        boolean[] q = {true, false, true, false};

        // Identify all unique operands used in the expressions
        String uniqueOperands = "";
        for (int i = 0; i < expr1.length(); i++) {
            char c = expr1.toLowerCase().charAt(i);
            if ("pqtfPQTF".contains(String.valueOf(c)) && uniqueOperands.indexOf(c) == -1) {
                uniqueOperands += c;
            }
        }
        for (int i = 0; i < expr2.length(); i++) {
            char c = expr2.toLowerCase().charAt(i);
            if ("pqtfPQTF".contains(String.valueOf(c)) && uniqueOperands.indexOf(c) == -1) {
                uniqueOperands += c;
            }
        }

        StringBuilder truthTable = new StringBuilder("<html><body><style>table {border-collapse: collapse;} th, td {padding: 10px;}</style><table><tr>");
        // Generate column headers for all unique operands
        for (int i = 0; i < uniqueOperands.length(); i++) {
            truthTable.append("<th>").append(uniqueOperands.toUpperCase().charAt(i)).append("</th>");
        }
        truthTable.append("<th>").append(expr1.toUpperCase()).append("</th><th>").append(expr2.toUpperCase()).append("</th></tr>");

        // Rest of the code remains the same
        for (int i = 0; i < 4; i++) {
            boolean val1 = evaluate(expr1, p[i], q[i]);
            boolean val2 = evaluate(expr2, p[i], q[i]);

            truthTable.append("<tr>");
            // Include values of unique operands
            for (int j = 0; j < uniqueOperands.length(); j++) {
                char operand = uniqueOperands.charAt(j);
                truthTable.append("<td>").append(operand == 'p' ? p[i] : operand == 'q' ? q[i] : operand == 't' ? true : false).append("</td>");
            }
            truthTable.append("<td>").append(val1).append("</td><td>").append(val2).append("</td></tr>");

            if (val1 != val2) {
                return "The expressions are not logically equivalent.<br/><br/>" + truthTable.toString() + "</table></body></html>";
            }
        }

        return "The expressions are logically equivalent.<br/><br/>" + truthTable.toString() + "</table></body></html>";
    }

    private boolean isValidExpression(String expression) {
        String validCharacters = "pqtfPQTF&V~-><->()";
        int openParentheses = 0;
        Stack<Character> stack = new Stack<>();
        boolean prevCharIsOperator = true; // Initial state assumes an operator.

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (validCharacters.indexOf(c) == -1) {
                return false; // Invalid character
            }

            if (c == '(') {
                if (!prevCharIsOperator) {
                    return false; // Missing operator between operands
                }
                openParentheses++;
                stack.push(c);
            } else if (c == ')') {
                if (openParentheses == 0 || prevCharIsOperator) {
                    return false; // Mismatched parentheses or missing operand
                }
                openParentheses--;
                stack.pop();
            } else if (c == 'p' || c == 'q' || c == 't' || c == 'f' || c == 'P' || c == 'Q' || c == 'T' || c == 'F') {
                if (!prevCharIsOperator) {
                    return false; // Missing operator between operands
                }
                prevCharIsOperator = false;
            } else { // It's an operator
                prevCharIsOperator = true;
            }
        }

        if (openParentheses != 0 || prevCharIsOperator) {
            return false; // Mismatched parentheses or missing operand
        }

        return true; // Valid expression
    }

    private boolean evaluate(String expr, boolean p, boolean q) {
        Stack<String> operators = new Stack<>();
        Stack<Boolean> operands = new Stack<>();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == 'p' || c == 'P') {
                operands.push(p);
            } else if (c == 'q' || c == 'Q') {
                operands.push(q);
            } else if (c == 't' || c == 'T') {
                operands.push(true);
            } else if (c == 'f' || c == 'F') {
                operands.push(false);
            } else if (c == '~') {
                operators.push(Character.toString(c));
            } else if (c == '(') {
                operators.push(Character.toString(c));
            } else if (c == ')') {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    applyOperator(operators, operands);
                }
                operators.pop(); // remove '(' from stack
            } else if (c == '-' && i + 1 < expr.length() && expr.charAt(i + 1) == '>') {
                // Handle '->' operator (implication)
                i++; // Skip the '>' part
                String op = "->";
                while (!operators.isEmpty() && precedence(op) <= precedence(operators.peek())) {
                    applyOperator(operators, operands);
                }
                operators.push(op);
            } else if (c == '<' && i + 2 < expr.length() && expr.charAt(i + 1) == '-' && expr.charAt(i + 2) == '>') {
                // Handle '<->' operator (equivalence)
                i += 2; // Skip the '->' part
                String op = "<->";
                while (!operators.isEmpty() && precedence(op) <= precedence(operators.peek())) {
                    applyOperator(operators, operands);
                }
                operators.push(op);
            } else { // Other operators
                String op = Character.toString(c);
                while (!operators.isEmpty() && precedence(op) <= precedence(operators.peek())) {
                    applyOperator(operators, operands);
                }
                operators.push(op);
            }
        }

        while (!operators.isEmpty()) {
            applyOperator(operators, operands);
        }

        return operands.pop();
    }

    private int precedence(String operator) {
        switch (operator) {
            case "~":
                return 4;
            case "&":
                return 3;
            case "V":
                return 2;
            case "->":
                return 1;
            case "<->":
                return 0;
            default:
                return -1;
        }
    }


    private void applyOperator(Stack<String> operators, Stack<Boolean> operands) {
        String operator = operators.pop();

        if (operator.equals("~")) { // unary operator
            boolean operand = operands.pop();
            operands.push(!operand);
        } else { // binary operator
            boolean operand2 = operands.pop();
            boolean operand1 = operands.pop();

            switch (operator) {
                case "&":
                    operands.push(operand1 && operand2);
                    break;
                case "V":
                    operands.push(operand1 || operand2);
                    break;
                case "->":
                    operands.push(!operand1 || operand2); // implication
                    break;
                case "<->":
                    operands.push(operand1 == operand2); // equivalence
                    break;
                default:
                    break;
            }
        }
    }
}
