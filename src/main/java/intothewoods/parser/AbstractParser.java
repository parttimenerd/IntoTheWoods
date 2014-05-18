package intothewoods.parser;

import intothewoods.lexer.AbstractLexer;
import intothewoods.lexer.LexerException;

import java.io.IOException;

/**
 * A parser turning lexer tokens into an AST (Abstract Syntax Tree).
 */
public abstract class AbstractParser {

	protected final AbstractLexer lexer;

	public AbstractParser(AbstractLexer lexer){
		this.lexer = lexer;
	}

	/**
	 * Parses the tokens from the lexer into a WHOLE_FILE rooted AST.
	 *
	 * The AST has two children, the GLOBAL node (has all global variable declaration as children)
	 * and the FUNCTION node (has all function declarations as children).
	 * The variable declaration nodes have COMMENT_BLOCK node as their last child, as well as the
	 * function declaration nodes. This COMMENT_BLOCK has the COMMENT tokens as its child that
	 * depend to its parent node.
	 *
	 * @return AST
	 * @throws intothewoods.parser.ParserException syntax error spotted by the parser
	 * @throws intothewoods.lexer.LexerException syntax error spotted by the lexer
	 * @throws java.io.IOException an IO error occurred in the lexer
	 */
	public abstract ASTNode parseTokens() throws ParserException, LexerException, IOException;

}
