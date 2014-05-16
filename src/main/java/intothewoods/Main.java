package intothewoods;

import intothewoods.common.TokenType;
import intothewoods.lexer.LexerToken;
import intothewoods.parser.ASTNode;

public class Main {

    public static void main(String[] args) throws Exception {
	    ASTNode node1 = new ASTNode(new LexerToken(TokenType.INT_LITERAL, "3", 1, 2));
	    ASTNode node2 = new ASTNode(new LexerToken(TokenType.INT_LITERAL, "4", 1, 2));
	    ASTNode node3 = new ASTNode(TokenType.EQUAL_SIGN);
	    node3.addChild(node1);
	    node3.addChild(node2);
	    System.out.println(node3.toStringTree());
    }
}
