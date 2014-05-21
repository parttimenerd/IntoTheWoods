package intothewoods.lexer;

import intothewoods.common.TokenType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Lexer turning an input stream into tokens.
 *
 * This is the main lexer implementation, it focuses on simplicity.
 */
public class BasicLexer extends AbstractLexer {

	public BasicLexer(InputStream input) throws LexerException, IOException {
		super(input);
	}

	/**
	 * Capture the next token from the input stream, stores and returns it.
	 *
	 * @return next token or token of type EOF if there is no next token
	 * @throws LexerException syntax error at the current input
	 * @throws java.io.IOException io error from stream
	 */
	public LexerToken nextToken() throws LexerException, IOException {
		while (isCurrentChar(' ') || isCurrentChar('\t') || isCurrentChar('\r')) {
			readChar();
		}
		if (hasEnded){
			token = new LexerToken(TokenType.EOF, "", currentLine, currentColumn);
		} else if (isCurrentChar('\n')) {
			token = new LexerToken(TokenType.NEW_LINE, "\n", currentLine, currentColumn);
			currentLine++;
			currentColumn = -1;
			codeLines.add(currentCodeLine.toString());
			currentCodeLine.setLength(0);
			readChar();
		} else if (isCurrentChar('_')) {
			token = parseKeyword();
		} else if (isCurrentChar('"')) {
			token = parseString();
		} else if (isCurrentChar('#')) {
			token = parseComment();
		} else if (Character.isDigit(getChar()) || isCurrentChar('+') || isCurrentChar('-')) {
			token = parseNumeric();
		} else if (Character.isAlphabetic(getChar())) {
			token = parseAlphaNumeric();
		} else {
			token = parseSingleTokens();
		}
		return token;
	}


	/**
	 * Lexes the stream into a keyword token.
	 *
	 * @return lexed token
	 * @throws LexerException invalid keyword
	 * @throws java.io.IOException io error from stream
	 */
	private LexerToken parseKeyword() throws LexerException, IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		int startColumn = currentColumn;
		do {
			readChar();
			if (Character.isLowerCase(getChar())) {
				builder.append(getChar());
			} else {
				break;
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
				throw createLexerException("Unknown keyword", startColumn);
		}
		return new LexerToken(type, tokenText, currentLine, startColumn);
	}

	/**
	 * Lexes the stream into a string token.
	 *
	 * @return lexed token
	 * @throws LexerException invalid string literal
	 * @throws java.io.IOException io error from stream
	 */
	private LexerToken parseString() throws LexerException, IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		int startColumn = currentColumn;
		do {
			readChar();
			builder.append(getChar());
			if (isCurrentChar('\\')) {
				readChar();
				if (isCurrentChar('n') || isCurrentChar('r') || isCurrentChar('t') ||
						isCurrentChar('"') || isCurrentChar('"')) {
					builder.append(getChar());
				} else {
					throw createLexerException("Unsupported escape character", startColumn);
				}
			} else if (isCurrentChar('"')){
				break;
			} else if (isCurrentChar('\n')){
				throw createLexerException("Strings mustn't contain new lines", startColumn);
			}
		} while (!hasEnded);
		char lastChar = getChar();
		String str = builder.toString();
		if (hasEnded && lastChar != '"') {
			throw createLexerException("Error at end of string", startColumn);
		}
		readChar();
		return new LexerToken(TokenType.STRING_LITERAL, str, currentLine, startColumn);
	}

	/**
	 * Lexes the stream into a comment token.
	 *
	 * @return lexed token
	 * @throws java.io.IOException io error from stream
	 */
	private LexerToken parseComment() throws IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		int startColumn = currentColumn;
		do {
			readChar();
			if (!hasEnded && !isCurrentChar('\n') && !isCurrentChar('\r')) {
				builder.append(getChar());
			} else {
				break;
			}
		} while (!hasEnded);
		return new LexerToken(TokenType.COMMENT, builder.toString(), currentLine, startColumn);
	}

	/**
	 * Lexes the stream into a numeric token.
	 *
	 * @return lexed token
	 * @throws intothewoods.lexer.LexerException syntax error
	 * @throws java.io.IOException io error from stream
	 * TODO: rewrite
	 */
	private LexerToken parseNumeric() throws IOException, LexerException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		int startColumn = currentColumn;
		boolean containsDot = false;
		boolean containsExp = false;
		boolean containsDigit = Character.isDigit(getChar());
		do {
			readChar();
			if (Character.isDigit(getChar()) ||
					(!containsDot && isCurrentChar('.')) ||
					(!containsExp && isCurrentChar('E'))) {
				builder.append(getChar());
				if (Character.isDigit(getChar())) {
					containsDigit = true;
				}
				if (!containsDot && isCurrentChar('.') && containsDigit) {
					containsDot = true;
					containsDigit = false;
				}
				if (!containsExp && isCurrentChar('E') && containsDigit) {
					containsExp = true;
					readChar();
					if (isCurrentChar('-') || isCurrentChar('+')){
						builder.append(getChar());
						containsDigit = false;
					} else if (Character.isDigit(getChar())){
						builder.append(getChar());
						containsDigit = true;
					} else {
						throw createLexerException("Invalid float literal", startColumn);
					}
				}
			} else {
				break;
			}
		} while (!hasEnded);
		TokenType type;
		if (!containsDigit){
			throw createLexerException("Invalid numeric literal", startColumn);
		}
		if (containsDot || containsExp){
			type = TokenType.FLOAT_LITERAL;
		} else {
			if (isCurrentChar('b')) {
				type = TokenType.BYTE_LITERAL;
				builder.append('b');
				readChar();
			} else {
				type = TokenType.INT_LITERAL;
			}
		}
		return new LexerToken(type, builder.toString(), currentLine, startColumn);
	}

	/**
	 * Lexes the stream into a name or bool token.
	 *
	 * @return lexed token
	 * @throws java.io.IOException io error from stream
	 */
	private LexerToken parseAlphaNumeric() throws IOException {
		StringBuilder builder = new StringBuilder(Character.toString(getChar()));
		int startColumn = currentColumn;
		do {
			readChar();
			if (Character.isAlphabetic(getChar()) || Character.isDigit(getChar()) || isCurrentChar('_')) {
				builder.append(getChar());
			} else {
				break;
			}
		} while (!hasEnded);
		if (isCurrentChar('!')){
			builder.append(getChar());
			readChar();
		}
		String text = builder.toString();
		TokenType type;
		switch (text){
			case "true":
			case "false":
				type = TokenType.BOOL_LITERAL;
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
		return new LexerToken(type, text, currentLine, startColumn);
	}

	/**
	 * Lexes the stream into a (left/right) parenthesis, a comma, a colon or an equal sign token.
	 *
	 * @return lexes token
	 * @throws LexerException illegal character
	 * @throws java.io.IOException io error from stream
	 */
	private LexerToken parseSingleTokens() throws LexerException, IOException {
		TokenType type;
		int startColumn = currentColumn;
		char curChar = getChar();
		switch (curChar){
			case '(':
				type = TokenType.LEFT_PARENTHESIS;
				break;
			case ')':
				type = TokenType.RIGHT_PARENTHESIS;
				break;
			case ',':
				type = TokenType.COMMA;
				break;
			case '=':
				type = TokenType.EQUAL_SIGN;
				break;
			case ':':
				type = TokenType.COLON;
				break;
			default:
				throw createLexerException("Illegal character", startColumn);
		}
		readChar();
		return new LexerToken(type, Character.toString(curChar), currentLine, startColumn);
	}

	/**
	 * Read a new char from the input stream and set it as the current char.
	 * @throws IOException read error
	 */
	private void readChar() throws IOException {
		int codePoint = input.read();
		currentColumn++;
		if (codePoint == -1){
			hasEnded = true;
		} else {
			currentCodeLine.appendCodePoint(codePoint);
		}
		currentChar = (char) codePoint;
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
	private boolean isCurrentChar(char otherChar) throws IOException {
		return getChar() == otherChar;
	}
}