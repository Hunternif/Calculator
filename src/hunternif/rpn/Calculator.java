package hunternif.rpn;

import hunternif.rpn.token.BasicFunction;
import hunternif.rpn.token.Token;
import hunternif.rpn.token.TokenBracket;
import hunternif.rpn.token.TokenComputable;
import hunternif.rpn.token.TokenConstant;
import hunternif.rpn.token.TokenFunction;
import hunternif.rpn.token.TokenOperator;
import hunternif.rpn.token.TokenSeparator;
import hunternif.rpn.token.TokenValue;
import hunternif.util.Tree;
import hunternif.util.Tree.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
	private static final Pattern numberPattern = Pattern.compile("\\A(\\d+(\\.\\d*)?).*");
	
	/** For 1-char tokens that can't overlap. */
	private static final List<Token> basicTokens = new ArrayList<>();
	/** For tokens that can overlap, e.g. "max" and "maxof3". */
	private static final List<Token> wordTokens = new ArrayList<>();
	
	public static void registerFunction(TokenFunction func) {
		wordTokens.add(func);
	}
	public static void registerConstant(TokenConstant constant) {
		wordTokens.add(constant);
	}
	
	static {
		for (TokenOperator op : TokenOperator.operators) {
			basicTokens.add(op);
		}
		for (TokenFunction func : BasicFunction.functions) {
			registerFunction(func);
		}
		registerConstant(new TokenConstant("pi", Math.PI));
		registerConstant(new TokenConstant("e", Math.E));
		basicTokens.add(TokenBracket.LEFT);
		basicTokens.add(TokenBracket.RIGHT);
		basicTokens.add(TokenSeparator.COMMA);
	}
	
	public static double calculate(String input) throws CalculationException {
		List<Token> tokens = tokenize(input);
		Tree<Token> tree = buildSyntaxTree(tokens);
		Stack<Token> rpnStack = tree.toStackPostOrder(); 
		return calculateRPN(rpnStack);
	}
	
	protected static List<Token> tokenize(String input) throws CalculationException {
		List<Token> tokens = new ArrayList<>();
		// Remove spaces:
		input = input.replaceAll("\\s", "");
		
		// Add the argument '0' to unary operator '-':
		input = input.replaceAll("\\A-", "0-");
		input = input.replaceAll("\\(-", "(0-");
		input = input.replaceAll(",-", ",0-");
		
		while (!input.isEmpty()) {
			boolean parsed = false;
			
			// Try number
			Matcher numberMatcher = numberPattern.matcher(input);
			if (numberMatcher.find()) {
				String found = numberMatcher.group(1);
				input = input.substring(found.length());
				double value = Double.parseDouble(found);
				tokens.add(new TokenValue(value));
				parsed = true;
			}
			
			// Try basic tokens: operators, brackets and commas
			for (Token token : basicTokens) {
				if (input.startsWith(token.notation)) {
					input = input.substring(token.notation.length());
					tokens.add(token);
					parsed = true;
					break;
				}
			}
			
			// Try word tokens: constants and functions.
			// As they can overlap, only apply token with the longest notation
			Token longestToken = null;
			for (Token token : wordTokens) {
				if (input.startsWith(token.notation)) {
					if (longestToken == null || longestToken.notation.length() < token.notation.length()) {
						longestToken = token;
					}
				}
			}
			if (longestToken != null) {
				input = input.substring(longestToken.notation.length());
				tokens.add(longestToken);
				parsed = true;
			}
			
			if (!parsed) {
				throw new CalculationException("Unknown expression: " + input);
			}
		}
		return tokens;
	}
	
	protected static Tree<Token> buildSyntaxTree(List<Token> list) throws CalculationException {
		Node<Token> headNode = new Node<Token>(null, null);
		for (Token curToken : list) {
			if (headNode.data == null) {
				headNode.data = curToken;
			} else {
				if (curToken == TokenBracket.LEFT) {
					if (headNode.data instanceof TokenComputable || headNode.data == TokenBracket.LEFT) {
						Node<Token> child = new Node<Token>(headNode, curToken);
						headNode.addChild(child);
						headNode = child;
					} else {
						throw new CalculationException("Unexpected opening bracket token");
					}
				} else if (curToken == TokenBracket.RIGHT) {
					// Go up until a matching opening bracket is found:
					while (headNode.data != TokenBracket.LEFT) {
						headNode = headNode.parent;
						if (headNode == null) {
							throw new CalculationException("Unexpected closing bracket token");
						}
					}
					if (headNode.parent != null && headNode.parent.data instanceof TokenComputable
							&& !((TokenComputable)headNode.parent.data).isInfix()) {
						Node<Token> parent = headNode.parent;
						parent.absorbNode(headNode);
						headNode = parent;
					} else {
						headNode.data = TokenBracket.COMPLETE;
					}
				} else if (curToken instanceof TokenValue) {
					if (headNode.data == TokenBracket.LEFT) {
						Node<Token> childNode = new Node<Token>(headNode, curToken);
						headNode.addChild(childNode);
						headNode = childNode;
					} else if (headNode.data instanceof TokenComputable) {
						Node<Token> childNode = new Node<Token>(headNode, curToken);
						headNode.addChild(childNode);
					} else {
						throw new CalculationException("Unexpected value token: " + curToken.notation);
					}
				} else if (curToken instanceof TokenComputable) {
					if (headNode.data instanceof TokenValue) {
						Node<Token> parentFunctionNode = new Node<Token>(null, curToken);
						headNode.insertParent(parentFunctionNode);
						headNode = parentFunctionNode;
					} else if (headNode.data == TokenBracket.LEFT) {
						Node<Token> childFunctionNode = new Node<Token>(null, curToken);
						headNode.addChild(childFunctionNode);
						headNode = childFunctionNode;
					} else if (headNode.data == TokenBracket.COMPLETE) {
						headNode.data = curToken;
					} else if (headNode.data instanceof TokenComputable) {
						TokenComputable headFunction = (TokenComputable)headNode.data;
						TokenComputable curFunction = (TokenComputable) curToken;
						if (headFunction.precedence < curFunction.precedence) {
							Node<Token> childFunctionNode = new Node<Token>(null, curFunction);
							if (curFunction.isInfix()) {
								Node<Token> childValueNode = headNode.children.get(headNode.children.size() - 1);
								headNode.children.remove(childValueNode);
								childFunctionNode.addChild(childValueNode);
							}
							headNode.addChild(childFunctionNode);
							headNode = childFunctionNode;
						} else {
							Node<Token> parentFunctionNode = new Node<Token>(null, curFunction);
							headNode.insertParent(parentFunctionNode);
							headNode = parentFunctionNode;
						}
					} else {
						throw new CalculationException("Unexpected function token: " + curToken.notation);
					}
				} else if (curToken == TokenSeparator.COMMA) {
					if (headNode.parent != null && headNode.parent.data == TokenBracket.LEFT) {
						headNode = headNode.parent;
					} else {
						throw new CalculationException("Unexpected comma token");
					}
				} else {
					throw new CalculationException("Unexpected token: " + curToken.notation);
				}
			}
		}
		// Find root:
		Tree<Token> tree = new Tree<Token>(null);
		while (headNode.parent != null) {
			headNode = headNode.parent;
		}
		tree.root = headNode;
		return tree;
	}
		
	protected static double calculateRPN(Stack<Token> baseRPNStack) throws CalculationException {
		Stack<Token> calcStack = new Stack<>();
		while (!baseRPNStack.isEmpty()) {
			Token node = baseRPNStack.pop();
			if (node instanceof TokenValue) {
				calcStack.push(node);
			} else if (node instanceof TokenComputable) {
				TokenComputable function = (TokenComputable) node;
				if (calcStack.size() < function.args) {
					throw new CalculationException("Wrong number of arguments " +
							calcStack.size() + " to function " + function.notation);
				}
				
				// Read params for the function from the stack
				double[] params = new double[function.args];
				for (int i = 0; i < function.args; i++) {
					Token paramNode = calcStack.pop();
					if (!(paramNode instanceof TokenValue)) {
						throw new CalculationException("Parameter node is not a value");
					}
					params[i] = ((TokenValue)paramNode).value;
				}
				
				// Push back into the stack the result of the function call
				double result = function.compute(params);
				calcStack.push(new TokenValue(result));
			}
		}
		if (calcStack.size() != 1) {
			throw new CalculationException("Stack size is " + calcStack.size());
		}
		Token node = calcStack.pop();
		if (!(node instanceof TokenValue)) {
			throw new CalculationException("Last node is not a value");
		}
		return ((TokenValue)node).value;
	}
}
