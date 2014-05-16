package intothewoods.parser;

import intothewoods.lexer.AbstractLexer;
import intothewoods.lexer.LexerException;

import java.io.IOException;

/**
 * A parser turning lexer tokens into an AST (Abstract Syntax Tree).
 */
public abstract class AbstractParser {

	protected AbstractLexer lexer;

	public AbstractParser(AbstractLexer lexer){
		this.lexer = lexer;
	}

	/**
	 * Parses the tokens from the lexer into a nil rooted AST.
	 * @return nil rooted AST
	 * @throws intothewoods.parser.ParserException syntax error spotted by the parser
	 * @throws intothewoods.lexer.LexerException syntax error spotted by the lexer
	 * @throws java.io.IOException an IO error occured in the lexer
	 */
	public abstract ASTNode parseTokens() throws ParserException, LexerException, IOException;

}
