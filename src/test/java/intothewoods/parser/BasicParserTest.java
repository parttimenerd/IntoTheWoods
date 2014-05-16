package intothewoods.parser;

import intothewoods.lexer.BasicLexer;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;

/**
 * Tests the BasicParser. It assumes that the BasicLexer works correctly.
 */
public class BasicParserTest extends TestCase {

	private BasicLexer lexer;
	private BasicParser parser;

	@Test
	public void testLiteralParsing() throws Exception {
		assertParseLiteral("3");
		assertParseLiteral("-3456");
		assertParseLiteral("\"absdf\t\\\"\"");
		assertParseLiteral("true");
		assertParseLiteral("3.7");
		assertParseLiteral("0b");
	}

	private void assertParseLiteral(String input) throws Exception {
		setInput(input);
		assertTreeEquals("Parsing literal failed", "(LITERAL " + input + ")", parser.parseLiteral(true));
	}

	private void setInput(String input) throws Exception {
		lexer = new BasicLexer(new ByteArrayInputStream(input.getBytes()));
		parser = new BasicParser(lexer);
	}

	private void assertTreeEquals(String message, String expectedTree, ASTNode actualTree) throws Exception {
		assertEquals(message, expectedTree, actualTree.toStringTree());
	}
}