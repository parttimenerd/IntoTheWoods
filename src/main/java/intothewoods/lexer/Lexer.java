package intothewoods.lexer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Lexer turning an input stream into tokens.
 * This is the main lexer implementation.
 */
public class Lexer extends AbstractLexer {

	private char currentChar = 0;
	private int currentLine = 1;
	private int currentColumn = -1;
	private boolean hasEnded = false;

	public Lexer(InputStream input) {
		super(input);
	}

	/**
	 * Capture the next token from the input stream and return it.
	 *
	 * @return next token or token of type EOF if there is no next token
	 * @throws intothewoods.lexer.SyntaxException syntax error at the current input
	 */
	public Token nextToken() throws SyntaxException, IOException {
		while (isChar(' ') || isChar('\t') || isChar('\r')) {
			readChar();
		}
		if (hasEnded){
			return new Token(TokenType.EOF, "", currentLine, currentColumn);
		}
		Token token;
		if (isChar('\n')) {
			token = new Token(TokenType.LINE_BREAK, "\n", currentLine, currentColumn);
			currentLine++;
			currentColumn = -1;
			readChar();
		} else if (isChar('_')) {
			token = parseKeyword();
		} else if (isChar('"')) {
			token = parseString();
		} else if (isChar('#')) {
			token = parseComment();
		} else if (Character.isDigit(getChar()) || isChar('+') || isChar('-') || isChar('.')) {
			token = parseNumeric();
		} else if (Character.isAlphabetic(getChar())) {
			token = parseNameAndTypeAndBool();
		} else {
			token = parseOther();
		}
		return token;
	}

	/**
	 * Parses the stream into a keyword token.
	 *
	 * @return parsed token
	 * @throws intothewoods.lexer.SyntaxException invalid keyword
	 */
	private Token parseKeyword() throws SyntaxException, IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		do {
			readChar();
			if (Character.isLowerCase(getChar())) {
				builder.append(getChar());
			}
		} while (!hasEnded);
		String tokenText = builder.toString();
		TokenType type;
		switch (tokenText) {
			case FUNCTION_KEYWORD:
				type = TokenType.FUNCTION_KEYWORD;
				break;
			case RETURN_KEYWORD:
				type = TokenType.RETURN_KEYWORD;
				break;
			case IF_KEYWORD:
				type = TokenType.IF_KEYWORD;
				break;
			case ELSE_KEYWORD:
				type = TokenType.ELSE_KEYWORD;
				break;
			case WHILE_KEYWORD:
				type = TokenType.WHILE_KEYWORD;
				break;
			case END_KEYWORD:
				type = TokenType.END_KEYWORD;
				break;
			default:
				throw new SyntaxException("Unknown keyword \"" + tokenText + "\".", currentLine, currentColumn);
		}
		return new Token(type, tokenText, currentLine, currentColumn);
	}

	/**
	 * Parses the stream into a string token.
	 *
	 * @return parsed token
	 * @throws intothewoods.lexer.SyntaxException invalid number of unescaped hyphens (> 2)
	 */
	private Token parseString() throws SyntaxException, IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		do {
			readChar();
			builder.append(getChar());
			if (isChar('\\')) {
				readChar();
				builder.append(getChar());
				readChar();
				builder.append(getChar());
			}
		} while (!hasEnded && !isChar('"'));
		char lastChar = getChar();
		String str = builder.toString();
		if (hasEnded && lastChar != '"') {
			throw new SyntaxException("Error at end of string '" + str + '\'', currentLine, currentColumn + str.length());
		}
		readChar();
		return new Token(TokenType.STRING, str, currentLine, currentColumn);
	}

	/**
	 * Parses the stream into a comment token.
	 *
	 * @return parsed token
	 */
	private Token parseComment() throws IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		do {
			readChar();
			if (!hasEnded && !isChar('\n') && !isChar('\r')) {
				builder.append(getChar());
			}
		} while (!hasEnded);
		return new Token(TokenType.COMMENT, builder.toString(), currentLine, currentColumn);
	}

	/**
	 * Parses the stream into a comment token.
	 *
	 * @return parsed token
	 */
	private Token parseNumeric() throws IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		boolean containsDot = false;
		do {
			readChar();
			if (Character.isDigit(getChar()) ||
					(builder.length() == 0 && (isChar('+') || isChar('-'))) ||
					(!containsDot && isChar('.'))) {
				builder.append(getChar());

				if (isChar('.')) {
					containsDot = true;
				}
			} else {
				break;
			}
		} while (!hasEnded);
		TokenType type;
		if (containsDot || builder.charAt(0) == '.'){
			type = TokenType.FLOAT;
		} else if (isChar('b')){
			type = TokenType.BYTE;
			builder.append('b');
			readChar();
		} else {
			type = TokenType.INT;
		}
		return new Token(type, builder.toString(), currentLine, currentColumn);
	}

	/**
	 * Parses the stream into a name or bool token.
	 *
	 * @return parsed token
	 */
	private Token parseNameAndTypeAndBool() throws IOException, SyntaxException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		do {
			readChar();
			if (Character.isAlphabetic(getChar()) || (builder.length() > 0 && (Character.isDigit(getChar()) || isChar('_')))) {
				builder.append(getChar());
			} else {
				break;
			}
		} while (!hasEnded);
		if (builder.length() < 1){
			throw new SyntaxException("Empty name is invalid, as well as the name \"!\"", currentLine, currentColumn);
		}
		if (isChar('!')){
			builder.append(getChar());
			readChar();
		}
		String text = builder.toString();
		TokenType type;
		switch (text){
			case "true":
			case "false":
				type = TokenType.BOOL;
				break;
			case "bool":
			case "byte":
			case "int":
			case "float":
			case "pointer":
			case "string":
				type = TokenType.TYPE;
				break;
			case "void":
				type = TokenType.VOID;
				break;
			default:
				type = TokenType.NAME;
		}
		return new Token(type, text, currentLine, currentColumn);
	}

	/**
	 * Parses the stream into a (left/right) parantheses, a comma or an equal sign token.
	 *
	 * @return parsed token
	 */
	private Token parseOther() throws IOException {
		TokenType type = TokenType.NONE;
		switch (getChar()){
			case '(':
				type = TokenType.LEFT_PARANTHESES;
				break;
			case ')':
				type = TokenType.RIGHT_PARANTHESES;
				break;
			case ',':
				type = TokenType.COMMA;
				break;
			case '=':
				type = TokenType.EQUAL_SIGN;
				break;
		}
		char otherChar = getChar();
		readChar();
		return new Token(type, Character.toString(otherChar), currentLine, currentColumn);
	}

	/**
	 * Read a new char from the input stream and return it. Set it as the current char.
	 * @return new current char
	 * @throws IOException read error
	 */
	private char readChar() throws IOException {
		int codePoint = input.read();
		currentColumn++;
		if (codePoint == -1){
			hasEnded = true;
		}
		currentChar = (char) codePoint;
		return currentChar;
	}

	/**
	 * Get the current char.
	 * @throws IOException read error
	 */
	private char getChar() throws IOException {
		if (currentChar == 0){
			readChar();
		}
		return currentChar;
	}

	/**
	 * Checks whether or not the current char is the given char.
	 * @throws IOException read error
	 */
	private boolean isChar(char otherChar) throws IOException {
		return getChar() == otherChar;
	}
}