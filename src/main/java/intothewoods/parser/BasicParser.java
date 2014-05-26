package intothewoods.parser;

import intothewoods.common.TokenType;
import intothewoods.lexer.AbstractLexer;
import intothewoods.lexer.LexerException;
import intothewoods.lexer.LexerToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser turning lexer tokens into an homogeneous AST.
 *
 * This is the main parser implementation, parsing the input line per line,
 * using a LL(2) parsing technique.
 */
public class BasicParser extends AbstractParser {

	private List<LexerToken> currentLine = new ArrayList<>();
	private boolean hadNewLineBefore = false;

	/**
	 * Initialize a BasicParser with the given lexer.
	 *
	 * @param lexer given lexer
	 * @throws intothewoods.lexer.LexerException lexer spotted a syntax error
	 * @throws java.io.IOException an IO error occurred in the lexer
	 */
	public BasicParser(AbstractLexer lexer) throws LexerException, IOException {
		super(lexer);
		readNextLine();
	}

	@Override
	public ASTNode parseTokens() throws ParserException, LexerException, IOException {
		ASTNode node = new ASTNode(TokenType.WHOLE_FILE);
		ASTNode functions = new ASTNode(TokenType.FUNCTIONS);
		ASTNode globals = new ASTNode(TokenType.GLOBALS);
		node.addChildren(globals, functions);
		LexerToken firstOfLine = currentLine.get(0);
		ASTNode currentNode = new ASTNode(TokenType.NIL);
		ASTNode commentNode = new ASTNode(TokenType.COMMENT_BLOCK);
		while (firstOfLine.hasNotType(TokenType.EOF)){
			switch (firstOfLine.getType()){
				case TYPE:
					currentNode = parseVariableDeclaration();
					globals.addChild(currentNode);
					break;
				case FUNCTION_KEYWORD:
					currentNode = parseFunctionDeclaration();
					functions.addChild(currentNode);
					break;
				case COMMENT:
					commentNode.addChild(firstOfLine);
					break;
				default:
					throw createParseException("Unexpected statement in global scope", firstOfLine);
			}
			if (firstOfLine.hasType(TokenType.TYPE) ||
					firstOfLine.hasType(TokenType.FUNCTION_KEYWORD)){
				currentNode.addChild(commentNode);
				commentNode = new ASTNode(TokenType.COMMENT_BLOCK);
			}
			readNextLine();
			if (hadNewLineBefore){
				commentNode = new ASTNode(TokenType.COMMENT_BLOCK);
			}
			firstOfLine = currentLine.get(0);
		}
		return node;
	}

	/**
	 * Parse the current block beginning with this line.
	 *
	 * A statement is a single block, as well as a control structure.
	 *
	 * @return AST node representing this block, or a NIL node if the current line is empty
	 * @throws ParserException parser spots a syntax error
	 * @throws intothewoods.lexer.LexerException lexer spots a syntax error
	 * @throws java.io.IOException an input error occurred in the lexer
	 */
	protected ASTNode parseCurrentLine() throws ParserException, LexerException, IOException {
		ASTNode node;
		LexerToken first = currentLine.get(0);
		switch (first.getType()){
			case IF_KEYWORD:
				node = parseCondition();
				break;
			case WHILE_KEYWORD:
				node = parseLoop();
				break;
			case NAME:
				if (currentLine.size() > 1 && currentLine.get(1).hasType(TokenType.EQUAL_SIGN)){
					node = parseVariableAssignment();
				} else {
					node = parseFunctionCall();
				}
				break;
			case TYPE:
				node = parseVariableDeclaration();
				break;
			case RETURN_KEYWORD:
				node = parseReturnStatement();
				break;
			case COMMENT:
				node = new ASTNode(TokenType.COMMENT);
				break;
			default:
				throw createParseException("Unknown statement", first);
		}
		return node;
	}

	/**
	 * Parses the current line as a variable declaration.
	 *
	 * The first token of the current line has to be a TYPE token.
	 *
	 * @return VARIABLE_DECLARATION AST node with the type token, the name token and the value token as children.
	 * @throws ParserException parser spots a syntax error
	 */
	protected ASTNode parseVariableDeclaration() throws ParserException {
		if (currentLine.size() != 4){
			throw createParseException("Expected variable declaration");
		}
		LexerToken typeToken = currentLine.get(0);
		LexerToken nameToken = currentLine.get(1);
		if (nameToken.hasNotType(TokenType.NAME)){
			throw createParseException("Expected variable name", typeToken);
		}
		if (currentLine.get(2).hasNotType(TokenType.EQUAL_SIGN)){
			throw createParseException("Expected '=' in variable declaration", currentLine.get(2));
		}
		ASTNode valueNode = parseTokenAsValue(currentLine.get(3));
		ASTNode node = new ASTNode(TokenType.VARIABLE_DECLARATION);
		node.addChildren(typeToken, nameToken);
		node.addChild(valueNode);
		return node;
	}

	/**
	 * Parses the current line as a variable assignment.
	 *
	 * The first token of the current line has to be a NAME token.
	 *
	 * @return VARIABLE_ASSIGNMENT AST node with the name token and the value token as children.
	 * @throws ParserException parser spots a syntax error
	 */
	protected ASTNode parseVariableAssignment() throws ParserException {
		if (currentLine.size() != 3){
			throw createParseException("Expected assignment");
		}
		LexerToken nameToken = currentLine.get(0);
		if (currentLine.get(1).hasNotType(TokenType.EQUAL_SIGN)){
			throw createParseException("Expected '=' in variable assignment", currentLine.get(1));
		}
		ASTNode valueNode = parseTokenAsValue(currentLine.get(2));
		ASTNode node = new ASTNode(TokenType.VARIABLE_ASSIGNMENT);
		node.addChild(nameToken);
		node.addChild(valueNode);
		return node;
	}

	/**
	 * Parses the current line (and the following) as a function declaration into an AST node.
	 *
	 * The AST node has the function header and a CODE_BLOCK node as its children.
	 *
	 * @return AST node
	 * @throws ParserException parser spots a syntax error
	 * @throws intothewoods.lexer.LexerException lexer spots a syntax error
	 * @throws java.io.IOException an input error occurred in the lexer
	 */
	protected ASTNode parseFunctionDeclaration() throws ParserException, LexerException, IOException {
		ASTNode func = new ASTNode(TokenType.FUNCTION_DECLARATION);
		ASTNode body = new ASTNode(TokenType.CODE_BLOCK);
		func.addChildren(parseFunctionHeader(), body);
		readNextLine();
		while (isNotEndLine()){
			body.addChild(parseCurrentLine());
			readNextLine();
		}
		return func;
	}

	/**
	 * Parses the current line as a function declaration header.
	 *
	 * The AST node has the type token, the name token and the PARAMETER_DECL_LIST node as its children.
	 * The last node (PARAMETER_DECL_LIST) is omitted, if the function hasn't any parameters.
	 * The first token of the current line has to be a FUNCTION_KEYWORD token.
	 *
	 * @return AST node
	 * @throws ParserException parser spots a syntax error
	 */
	protected ASTNode parseFunctionHeader() throws ParserException {
		if (currentLine.size() < 3){
			throw createParseException("Expected function header");
		}
		LexerToken typeToken = currentLine.get(1);
		if (typeToken.hasNotType(TokenType.TYPE) && typeToken.hasNotType(TokenType.VOID)){
			throw createParseException("Expected return type", typeToken);
		}
		LexerToken nameToken = currentLine.get(2);
		if (nameToken.hasNotType(TokenType.NAME)){
			throw createParseException("Expected function name", nameToken);
		}
		ASTNode node = new ASTNode(TokenType.FUNCTION_HEADER);
		node.addChild(typeToken);
		node.addChild(nameToken);
		if (currentLine.size() > 3){
			if (currentLine.get(3).hasType(TokenType.COLON)){
				node.addChild(parseParameterDeclarationList());
			} else {
				throw createParseException("Expected colon and function parameter declarations", currentLine.get(3));
			}
		}
		return node;
	}

	/**
	 * Parses the current line as a function parameter list.
	 *
	 * @return PARAMETER_DECL_LIST AST node with the PARAMETER nodes (containing a type and a name) as its children.
	 * @throws ParserException parser spots a syntax error
	 */
	private ASTNode parseParameterDeclarationList() throws ParserException {
		ASTNode parameters = new ASTNode(TokenType.PARAMETER_DECL_LIST);
		int i = 3;
		int paramNumber = 1;
		while (true){
			LexerToken lastOfLine = currentLine.get(currentLine.size() - 1);
			if (i != 3 && currentLine.get(i).hasNotType(TokenType.COMMA)) {
				throw createParseException("Expected ',' and parameter declaration", currentLine.get(i));
			}
			if (currentLine.size() <= i + 1){
				throw createParseException("Expected type of parameter no. " + paramNumber, lastOfLine);
			}
			if (currentLine.get(i + 1).hasNotType(TokenType.TYPE)){
				throw createParseException("Expected type of parameter no. " + paramNumber, currentLine.get(i + 1));
			}
			if (currentLine.size() <= i + 2){
				throw createParseException("Expected name of parameter no. " + paramNumber, lastOfLine);
			}
			if (currentLine.get(i + 2).hasNotType(TokenType.NAME)){
				throw createParseException("Expected name of parameter no. " + paramNumber, currentLine.get(i + 2));
			}
			ASTNode parameter = new ASTNode(TokenType.PARAMETER_DECL);
			parameter.addChildren(currentLine.get(i + 1), currentLine.get(i + 2));
			parameters.addChild(parameter);
			if (currentLine.size() <= i + 3){
				break;
			}
			i += 3;
		}
		return parameters;
	}

	/**
	 * Parses the current line as a function call.
	 *
	 * The first token of the current line has to be a NAME token.
	 *
	 * @return FUNCTION_CALL AST node with the name token and the parameter VALUE nodes as its children.
	 * @throws ParserException parser spots a syntax error
	 */
	protected ASTNode parseFunctionCall() throws ParserException {
		ASTNode node = new ASTNode(TokenType.FUNCTION_CALL);
		node.addChild(currentLine.get(0));
		for (int i = 1; i < currentLine.size(); i++){
			node.addChild(parseTokenAsValue(currentLine.get(i)));
		}
		return node;
	}

	/**
	 * Parses the current line as a return statement.
	 *
	 * The first token of the current line has to be a RETURN_KEYWORD token.
	 *
	 * @return RETURN AST node with the return value (VALUE node) as its token
	 * @throws ParserException parser spots a syntax error
	 */
	protected ASTNode parseReturnStatement() throws ParserException {
		if (currentLine.size() > 2){
			throw createParseException("Expected return statement");
		}
		ASTNode node = new ASTNode(TokenType.RETURN_STATEMENT);
		if (currentLine.size() == 2) {
			LexerToken valueToken = currentLine.get(1);
			node.addChild(parseTokenAsValue(valueToken));
		}
		return node;
	}

	/**
	 * Parses the current line (and the following) as a loop into an AST node.
	 *
	 * The AST node has the loop parameter VALUE node and the CODE_BLOCK node as its children.
	 * The first token of the current line has to be a WHILE_KEYWORD token.
	 *
	 * @return AST node
	 * @throws ParserException parser spots a syntax error
	 * @throws intothewoods.lexer.LexerException lexer spots a syntax error
	 * @throws java.io.IOException an input error occurred in the lexer
	 */
	protected ASTNode parseLoop() throws ParserException, LexerException, IOException {
		if (currentLine.size() != 2){
			throw createParseException("Expected loop");
		}
		ASTNode loop = new ASTNode(TokenType.LOOP);
		ASTNode parameter = parseTokenAsValue(currentLine.get(1));
		ASTNode body = new ASTNode(TokenType.CODE_BLOCK);
		loop.addChildren(parameter, body);
		readNextLine();
		while (isNotEndLine()){
			body.addChild(parseCurrentLine());
			readNextLine();
		}
		return loop;
	}

	/**
	 * Parses the current line (and the following) as a condition into an AST node.
	 *
	 * The AST node has the loop parameter VALUE node, the "if" CODE_BLOCK and
	 * the "else" CODE_BLOCK node as its children.
	 * Expects the first token of the current line to be an IF_KEYWORD token.
	 *
	 * @return AST node
	 * @throws ParserException parser spots a syntax error
	 * @throws intothewoods.lexer.LexerException lexer spots a syntax error
	 * @throws java.io.IOException an input error occurred in the lexer
	 */
	protected ASTNode parseCondition() throws ParserException, LexerException, IOException {
		if (currentLine.size() != 2){
			throw createParseException("Expected condition");
		}
		ASTNode condition = new ASTNode(TokenType.CONDITION);
		ASTNode parameter = parseTokenAsValue(currentLine.get(1));
		ASTNode ifBody = new ASTNode(TokenType.CODE_BLOCK);
		condition.addChildren(parameter, ifBody);
		readNextLine();
		while (isNotEndLine() && !isElseLine()){
			ifBody.addChild(parseCurrentLine());
			readNextLine();
		}
		if (isElseLine()){
			ASTNode elseBody = new ASTNode(TokenType.CODE_BLOCK);
			condition.addChild(elseBody);
			readNextLine();
			while (isNotEndLine()){
				elseBody.addChild(parseCurrentLine());
				readNextLine();
			}
		}
		return condition;
	}

	/**
	 * Parses the given as a value (a name or a literal).
	 *
	 * @param token given token
	 * @return VALUE ast node with the actual name or literal as its child node.
	 * @throws ParserException parser spots a syntax error
	 */
	protected ASTNode parseTokenAsValue(LexerToken token) throws ParserException {
		if (token.hasNotType(TokenType.NAME) && !token.isLiteral()){
			throw createParseException("Expected a variable name or literal", token);
		}
		return new ASTNode(TokenType.VALUE, new ASTNode(token));
	}

	/**
	 * Checks wether or not the current line only contains the end keyword.
	 * @return does the the current line not only contains the end keyword?
	 */
	private boolean isNotEndLine(){
		return !isSingleTokenLine(TokenType.END_KEYWORD);
	}

	/**
	 * Checks wether or not the current line only contains the else keyword.
	 * @return does the the current line only contain the else keyword?
	 */
	private boolean isElseLine(){
		return isSingleTokenLine(TokenType.ELSE_KEYWORD);
	}

	/**
	 * Read a new line of tokens from the lexer.
	 *
	 * Skips new lines (if it skips and empty line, it sets hadNewLineBefore to true, otherwise to false).
	 * Adds an EOF token to last line if it's empty.
	 *
	 * @throws intothewoods.lexer.LexerException lexer spots a syntax error
	 * @throws java.io.IOException an input error occurred in the lexer
	 */
	private void readNextLine() throws LexerException, IOException {
		LexerToken token = lexer.getToken();
		currentLine.clear();
		hadNewLineBefore = false;
		while (token.hasType(TokenType.NEW_LINE)){
			hadNewLineBefore = true;
			token = lexer.nextToken();
		}
		while (!token.hasType(TokenType.NEW_LINE) && !token.hasType(TokenType.EOF)){
			currentLine.add(token);
			token = lexer.nextToken();
		}
		if (token.hasType(TokenType.EOF) && currentLine.isEmpty()){
			currentLine.add(token);
		}
	}

	private boolean isSingleTokenLine(TokenType type){
		return currentLine.size() == 1 && currentLine.get(0).hasType(type);
	}

	private ParserException createParseException(String message, LexerToken problematicToken){
		return new ParserException(message, problematicToken, lexer.getLine(problematicToken.getLine()));
	}

	private ParserException createParseException(String message){
		if (currentLine.isEmpty()){
			return new ParserException(message, new LexerToken(TokenType.NIL, "", 0, 0), "");
		}
		return new ParserException(message, currentLine.get(0), lexer.getLine(currentLine.get(0).getLine()));
	}

}
