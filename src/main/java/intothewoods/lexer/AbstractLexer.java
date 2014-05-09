package intothewoods.lexer;

import java.io.IOException;
import java.io.InputStream;

/**
 * /**
 * Lexer turning an input stream into tokens, ignoring every sort of white space.
 */
public abstract class AbstractLexer {

	protected static final String FUNCTION_KEYWORD = "_function";
	protected static final String RETURN_KEYWORD = "_return";
	protected static final String IF_KEYWORD = "_if";
	protected static final String ELSE_KEYWORD = "_else";
	protected static final String WHILE_KEYWORD = "_while";
	protected static final String END_KEYWORD = "_end";

	protected InputStream input;

	/**
	 * Construct a lexer reading from the given stream.
	 *
	 * @param input given input stream
	 */
	public AbstractLexer(InputStream input) {
		this.input = input;
	}

	/**
	 * Capture the next token from the input stream and return it.
	 * @return next token or token of type EOF if there is no next token
	 * @throws intothewoods.lexer.SyntaxException syntax error
	 */
	public abstract Token nextToken() throws SyntaxException, IOException;

}
