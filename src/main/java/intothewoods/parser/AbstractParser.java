package intothewoods.parser;

import intothewoods.lexer.AbstractLexer;

/**
 * A parser turning lexer tokens into an AST (Abstract Syntax Tree).
 */
public abstract class AbstractParser {

	private AbstractLexer lexer;
	private ASTNode currentNode;

	public AbstractParser(AbstractLexer lexer){
		this.lexer = lexer;
	}

	/**
	 * Parses the tokens from the lexer into a nil rooted AST.
	 * @return nil rooted AST
	 */
	public abstract ASTNode parseTokens();

}
