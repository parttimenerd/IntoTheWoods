package intothewoods.lexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer turning an input stream into tokens.
 */
public abstract class AbstractLexer {

	protected static final String FUNCTION_KEYWORD = "_function";
	protected static final String RETURN_KEYWORD = "_return";
	protected static final String IF_KEYWORD = "_if";
	protected static final String ELSE_KEYWORD = "_else";
	protected static final String WHILE_KEYWORD = "_while";
	protected static final String END_KEYWORD = "_end";


	protected char currentChar;
	protected int currentLine;
	protected int currentColumn;
	protected boolean hasEnded;
	protected final InputStream input;
	protected LexerToken token;
	protected List<String> codeLines = new ArrayList<>();
	protected StringBuilder currentCodeLine = new StringBuilder();

	/**
	 * Initializes a lexer reading from the given stream.
	 *
	 * Reads the first token from the stream.
	 *
	 * @param input given input stream
	 * @throws intothewoods.lexer.LexerException syntax error
	 * @throws java.io.IOException io error from stream
	 */
	public AbstractLexer(InputStream input) throws LexerException, IOException {
		this.input = input;
		currentChar = 0;
		currentLine = 1;
		currentColumn = -1;
		hasEnded = false;
		nextToken();
	}

	/**
	 * Capture the next token from the input stream, stores and returns it.
	 *
	 * @return next token or token of type EOF if there is no next token
	 * @throws intothewoods.lexer.LexerException syntax error
	 * @throws java.io.IOException io error from stream
	 */
	public abstract LexerToken nextToken() throws LexerException, IOException;

	/**
	 * Return the current token.
	 * @return current token
	 */
	public LexerToken getToken(){
		return token;
	}

	/**
	 * Create a new LexerException for the current line and column number.
	 * This method is the best way to create a LexerException.
	 *
	 * @param message error message
	 * @param column starting column of the mismatched token
	 * @return new lexer exception
	 */
	protected LexerException createLexerException(String message, int column){
		return new LexerException(message, currentLine, column, getLine(currentLine));
	}

	/**
	 * Create a new LexerException for the current line and column number.
	 * This method is the best way to create a LexerException.
	 *
	 * @param message error message
	 * @return new lexer exception
	 */
	protected LexerException createLexerException(String message){
		return new LexerException(message, currentLine, currentColumn, getLine(currentLine));
	}

	/**
	 * Returns the code line with the given index or an empty string if the requested
	 * line hasn't been read from the input.
	 *
	 * @param index given index (starting with 1)
	 * @return the requested line
	 */
	public String getLine(int index) {
		if (index <= codeLines.size()) {
			return codeLines.get(index - 1);
		} else if (index == codeLines.size() + 1){
			return currentCodeLine.toString();
		} else {
			return "";
		}
	}
}
