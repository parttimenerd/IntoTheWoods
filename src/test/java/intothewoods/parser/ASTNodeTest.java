package intothewoods.parser;

import intothewoods.common.Token;
import intothewoods.common.TokenType;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Tests the ASTNode class, especially it's toStringTree() method.
 */
public class ASTNodeTest extends TestCase {

	@Test
	public void testToStringTree() throws Exception {
		ASTNode node = new ASTNode(TokenType.EQUAL_SIGN);
		node.addChild(new ASTNode(new Token(TokenType.INT_LITERAL, "3")));
		node.addChild(new ASTNode(new Token(TokenType.INT_LITERAL, "4")));
		assertEquals("Stringify simple tree", "(EQUAL_SIGN 3 4)", node.toStringTree());
	}

	@Test
	public void testToStringTreeNilTree() throws Exception {
		ASTNode node = new ASTNode(TokenType.NIL);
		node.addChild(new ASTNode(new Token(TokenType.INT_LITERAL, "3")));
		node.addChild(new ASTNode(new Token(TokenType.INT_LITERAL, "4")));
		assertEquals("Stringify simple tree", "3 4", node.toStringTree());
	}

}