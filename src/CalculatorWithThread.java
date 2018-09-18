// stress-made by Cristopher
// 2017730017
// well, remind me if you find any funny bugs!

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.Stack;

public class CalculatorWithThread {
	public static void main (String[] args) {
		Thread scanner = new Thread(new ScannerThread());
		scanner.start();
	}
}

class ScannerThread implements Runnable {
	@Override
	public void run() {
		System.out.println("STARTING CALCULATOR... (ALSO SUPPORTS MULTIPLE EXPRESSIONS!)");
		System.out.println("This calculator currently supports : '+' '-' '/' 'x' ':' (is the same as '/') and parentheses");
		System.out.println("This cool calculator supports floating value calculations!");
		System.out.print("Number of expressions to evaluate : ");
		BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
		int cases = 0;
		System.out.println();
		try {
			cases = Integer.parseInt(rd.readLine());
		} catch (IOException e) {
			System.out.println("I/O exception thrown, terminating program...");
			e.printStackTrace();
			System.exit(1);
		}
		while(cases-->0) {
			try {
				String expression = rd.readLine();
				Thread validate = new Thread(new ValidatorThread(expression));
				validate.start();
			} catch (IOException e) {
				System.out.println("I/O exception thrown, terminating program...");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}

class ValidatorThread implements Runnable {
	public String exp;
	
	public ValidatorThread (String exp) {
		this.exp = exp;
	}
	
	@Override
	public void run () {
		if(Pattern.matches("[^0-9+-/*^():]", this.exp) || this.exp.isEmpty()) { // apabila ada sesuatu yang bukan angka
			System.out.println("Invalid Expressions!");						    // atau bukan operator atau expression kosong, kill()
		} else {
			boolean number = true;
			int it = 0;
			int parentheses = 0;
			char last = this.exp.charAt(it);
			while(it<this.exp.length()) {
				char eval = this.exp.charAt(it);
				if(eval == ' ') it++;
				else if (eval == '(') {
					if(!number) {
						System.out.println("Invalid Expressions!");
						return;
					}
					else parentheses++;
					it++;
				}
				else if (eval == ')') {
					if(parentheses == 0 || !this.isNumber(last)) {
						System.out.println("Invalid Expressions!");
						return;
					}
					else parentheses--;
					it++;
				}
				else {
					if(this.isNumber(eval)) {
						if(!number) {
							System.out.println("Invalid Expressions!");
							return;
						}
						else {
							it = this.getNumberLimit(it);
							number = false;
						}
					} else {
						if(number) {
							System.out.println("Invalid Expressions!");
							return;
						}
						else {
							it++;
							number = true;
						}
					}
					last = eval;
				}
			}
			if(!this.isNumber(last) || parentheses != 0) {
				System.out.println("Invalid Expressions!");
				return;
			}
			else {
				Thread evaluator = new Thread(new EvaluatorThread(this.exp));
				evaluator.start();
			}
		}
	}
	
	public int getNumberLimit (int idx) {
		int end = idx+1;
		while(end < this.exp.length() && this.isNumber(this.exp.charAt(end))) end++;
		return end;
	}
	
	public boolean isNumber(char num) {
		return num >= 48 && num <= 57;
	}
}

class EvaluatorThread implements Runnable {
	public String exp;
	
	public EvaluatorThread (String exp) {
		this.exp = exp;
	}
	
	@Override
	public void run() {
		// IMPLEMENTED STUNTING YARD ALGORITHM BY DIJKSTRA
		// Of course, all credits goes to GeeksforGeeks!
		Stack<Character> operator = new Stack<>();
		Stack<Double> operand = new Stack<>();
		int it = 0;
		while(it<this.exp.length()) {
			char curr = this.exp.charAt(it);
			if(this.isNumber(curr)) {
				int limit = this.getNumberLimit(it);
				double num = Double.parseDouble(this.exp.substring(it, limit));
				it = limit;
				operand.push(num);
			} else {
				if(curr == '(') operator.push(curr);
				else if (curr == ')') {
					while(operator.peek() != '(') {
						char operate = operator.pop();
						double first = operand.pop();
						double second = operand.pop();
						if((operate == '/' || operate == ':') && first == 0) {
							System.out.println("ERROR : Divide by zero detected!");
							return;
						}
						operand.push(this.calculate(second, first, operate));
					}
					operator.pop();
				} else if(curr != ' ') {
					while(!operator.isEmpty() && this.hasPrecedence(curr, operator.peek()) && operand.size() >= 2) {
						char operate = operator.pop();
						double first = operand.pop();
						double second = operand.pop();
						if((operate == '/' || operate == ':') && first == 0) {
							System.out.println("ERROR : Divide by zero detected!");
							return;
						}
						operand.push(this.calculate(second, first, operate));
					}
					operator.push(curr);
				}
				it++;
			}
		}
		while(!operator.isEmpty() && operand.size() >= 2) {
			char operate = operator.pop();
			double first = operand.pop();
			double second = operand.pop();
			if((operate == '/' || operate == ':') && first == 0) {
				System.out.println("ERROR : Divide by zero detected!");
				return;
			}
			operand.push(this.calculate(second, first, operate));
		}
		double result = operand.pop();
		if(Math.floor(result) == result) System.out.println((int)result);
		else System.out.printf("%.2f (note : rounded to 2 decimal places)\n", result);
	}
	
	// true if op2 precedence is same or higher than op1, false otherwise
	// credits : GeeksforGeeks
	// MODIFIED TO SUPPORT ^ OPERATOR
	public boolean hasPrecedence(char op1, char op2) { 
		if(op2 == '^') return true;
		if(op1 == '^' && op2 != '^') return false;
        if (op2 == '(' || op2 == ')') return false; 
        if ((op1 == '*' || op1 == '/' || op1 == ':') && (op2 == '+' || op2 == '-')) return false; 
        return true; 
    } 
	
	public int getNumberLimit (int idx) {
		int end = idx+1;
		while(end < this.exp.length() && this.isNumber(this.exp.charAt(end))) end++;
		return end;
	}
	
	public boolean isNumber(char num) {
		return num >= 48 && num <= 57;
	}
	
	public double calculate(double first, double second, char operator) {
		switch (operator) {
			case '+' : return first+second;
			case '-' : return first-second;
			case '*' : return first*second;
			case '/' : return first/second;
			case ':' : return first/second;
			default : return Math.pow(first, second);
		}
	}
}