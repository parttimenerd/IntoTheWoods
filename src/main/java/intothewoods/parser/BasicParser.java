package intothewoods.parser;

import intothewoods.common.TokenType;
import intothewoods.lexer.AbstractLexer;
import intothewoods.lexer.LexerException;
import intothewoods.lexer.LexerToken;;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Parser turning lexer tokens into an homogeneous AST.
 * This is the main parser implementation, it's a LL(2) parser
 * and has threfore has a look ahead of two tokens.
 */
public class BasicParser extends AbstractParser {

	private LexerToken currentToken;
	private LexerToken nextToken;

	/**
	 * Initialize a BasicParser with the given lexer.
	 * @param lexer given lexer
	 * @throws intothewoods.lexer.LexerException lexer spotted a syntax error
	 * @throws java.io.IOException an IO error occurred in the lexer
	 */
	public BasicParser(AbstractLexer lexer) throws LexerException, IOException {
		super(lexer);
		currentToken = lexer.getToken();
		nextToken = lexer.nextToken();
	}

	@Override
	public ASTNode parseTokens() throws ParserException, LexerException, IOException {
		try {
			parseLiteral(true);
		} catch (ParseFlowException flowEx){
			throw new ParserException(flowEx.getMessage(), flowEx.getToken());
		}
		return null;
	}

	/**
	 * Turns the next token into a LITERAL AST node.
	 * A LITERAL node has a token node (with the actual token) as its child.
	 * @param readToken does this method pull a new token from the lexer after it created an ASTNode?
 	 * @return LITERAL AST node
	 * @throws intothewoods.parser.ParseFlowException the token isn't a literal
	 * @throws intothewoods.lexer.LexerException the lexer spots a syntax error
	 * @throws java.io.IOException an IO error occured in the lexer
	 */
	public ASTNode parseLiteral(boolean readToken) throws ParseFlowException, LexerException, IOException {
		if (isLiteral()){
			ASTNode node = new ASTNode(TokenType.LITERAL, new ASTNode(currentToken));
			readToken(readToken);
			return node;
		}
		throw parseFlowException("Expected literal");
	}

	private boolean isLiteral(){
		return currentToken.hasType(TokenType.BOOL_LITERAL) ||
				currentToken.hasType(TokenType.BYTE_LITERAL) ||
				currentToken.hasType(TokenType.INT_LITERAL) ||
				currentToken.hasType(TokenType.FLOAT_LITERAL) ||
				currentToken.hasType(TokenType.STRING_LITERAL);
	}

	private void readToken() throws LexerException, IOException {
		currentToken = nextToken;
		nextToken = lexer.nextToken();
	}

	/**
	 * Convenience method. Only reads a token if readToken is true.
	 * Equivalent to `if (readToken) readToken()`.
	 * @param readToken does this method actually read a token?
	 */
	private void readToken(boolean readToken) throws LexerException, IOException{
		if (readToken){
			readToken();
		}
	}

	private boolean currentHasType(TokenType type){
		return currentToken.hasType(type);
	}

	private ParseFlowException parseFlowException(String message){
		return ParseFlowException.getException(message, currentToken);
	}

}
