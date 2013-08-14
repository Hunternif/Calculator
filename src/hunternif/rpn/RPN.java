package hunternif.rpn;

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
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RPN {
	private static Pattern numberPattern = Pattern.compile("\\A(-?\\d+(\\.\\d*)?).*");
	private static Pattern operatorPattern = Pattern.compile("\\A([-/\\+\\*\\^]).*");
	private static Pattern wordPattern = Pattern.compile("\\A([^\\(\\)\\d-/\\+\\*\\^]+).*");
	
	protected static Map<String, TokenOperator> operatorMap = new ConcurrentHashMap<>();
	protected static Map<String, TokenConstant> constantMap = new ConcurrentHashMap<>();
	protected static Map<String, TokenFunction> functionMap = new ConcurrentHashMap<>();
	
	public static void registerOperator(TokenOperator op) {
		operatorMap.put(op.notation, op);
	}
	public static void registerFunction(TokenFunction func) {
		functionMap.put(func.notation, func);
	}
	public static void registerConstant(TokenConstant constant) {
		constantMap.put(constant.notation, constant);
	}
	
	static {
		for (TokenOperator op : TokenOperator.operators) {
			registerOperator(op);
		}
		registerConstant(new TokenConstant("pi", Math.PI));
		registerConstant(new TokenConstant("e", Math.E));
	}
	
	public static double calculate(String input) throws RPNCalculationException {
		List<Token> tokens = tokenize(input);
		Tree<Token> tree = buildSyntaxTree(tokens);
		Stack<Token> rpnStack = tree.toStackPostOrder(); 
		return calculateRPN(rpnStack);
	}
	
	protected static List<Token> tokenize(String input) throws RPNCalculationException {
		List<Token> tokens = new ArrayList<>();
		// Remove spaces:
		input = input.replaceAll("\\s", "");
		
		int bracketCount = 0;
		while (!input.isEmpty()) {
			// Try number
			Matcher numberMatcher = numberPattern.matcher(input);
			if (numberMatcher.find()) {
				String found = numberMatcher.group(1);
				input = input.substring(found.length());
				double value = Double.parseDouble(found);
				tokens.add(new TokenValue(value));
			}
			
			//Try operator
			Matcher opMatcher = operatorPattern.matcher(input);
			if (opMatcher.find()) {
				String found = opMatcher.group(1);
				input = input.substring(found.length());
				Token token = operatorMap.get(found);
				if (token == null) {
					throw new RPNCalculationException("Unknown operator: " + found);
				}
				tokens.add(token);
			}
			
			// Try word, which could be a constant or a function (only binary operators so far)
			Matcher wordMatcher = wordPattern.matcher(input);
			if (wordMatcher.find()) {
				String found = wordMatcher.group(1);
				input = input.substring(found.length());
				Token token = functionMap.get(found);
				if (token == null) {
					token = constantMap.get(found);
					if (token == null) {
						throw new RPNCalculationException("Unknown expression: " + found);
					}
				}
				tokens.add(token);
			}
			// Try bracket
			if (input.startsWith("(")) {
				bracketCount++;
				input = input.substring(1);
				tokens.add(TokenBracket.LEFT);
			} else if (input.startsWith(")")) {
				if (bracketCount == 0) {
					throw new RPNCalculationException("Invalid closing bracket at: " + input);
				}
				bracketCount--;
				input = input.substring(1);
				tokens.add(TokenBracket.RIGHT);
			} else if (input.startsWith(",")) {
				//FIXME test this comma
				if (bracketCount == 0) {
					throw new RPNCalculationException("Invalid comma at: " + input);
				}
				input = input.substring(1);
				tokens.add(TokenSeparator.COMMA);
			}
		}
		return tokens;
	}
	
	protected static Tree<Token> buildSyntaxTree(List<Token> list) throws RPNCalculationException {
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
						throw new RPNCalculationException("Unexpected opening bracket token");
					}
				} else if (curToken == TokenBracket.RIGHT) {
					// Go up until a matching opening bracket is found:
					while (headNode.data != TokenBracket.LEFT) {
						headNode = headNode.parent;
						if (headNode == null) {
							throw new RPNCalculationException("Unexpected closing bracket token");
						}
					}
					if (headNode.parent != null && headNode.parent.data instanceof TokenComputable
							&& !((TokenComputable)headNode.parent.data).isInfix()) {
						//FIXME test this non-infix function parsing
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
						throw new RPNCalculationException("Unexpected value token: " + curToken.notation);
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
						throw new RPNCalculationException("Unexpected function token: " + curToken.notation);
					}
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
		
	protected static double calculateRPN(Stack<Token> baseRPNStack) throws RPNCalculationException {
		Stack<Token> calcStack = new Stack<>();
		while (!baseRPNStack.isEmpty()) {
			Token node = baseRPNStack.pop();
			if (node instanceof TokenValue) {
				calcStack.push(node);
			} else if (node instanceof TokenComputable) {
				TokenComputable function = (TokenComputable) node;
				if (calcStack.size() < function.args) {
					throw new RPNCalculationException("Wrong number of arguments " +
							calcStack.size() + " to function " + function.notation);
				}
				
				// Read params for the function from the stack
				double[] params = new double[function.args];
				for (int i = 0; i < function.args; i++) {
					Token paramNode = calcStack.pop();
					if (!(paramNode instanceof TokenValue)) {
						throw new RPNCalculationException("Parameter node is not a value");
					}
					params[i] = ((TokenValue)paramNode).value;
				}
				
				// Push back into the stack the result of the function call
				double result = function.compute(params);
				calcStack.push(new TokenValue(result));
			}
		}
		if (calcStack.size() != 1) {
			throw new RPNCalculationException("Stack size is " + calcStack.size());
		}
		Token node = calcStack.pop();
		if (!(node instanceof TokenValue)) {
			throw new RPNCalculationException("Last node is not a value");
		}
		return ((TokenValue)node).value;
	}
}
