package intothewoods.lexer;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Test the main lexer implementation.
 */
public class LexerTest {

	private Lexer lexer;

	@Test
	public void testBoolLiteral() throws Exception {
		expectToken("Parsing true failed", "true", TokenType.BOOL);
		expectToken("Parsing false failed", "false", TokenType.BOOL);
	}

	@Test
	public void testByteLiteral() throws Exception {
		expectToken("Parsing byte failed", "0b", TokenType.BYTE);
		expectToken("Parsing byte failed", "-3435b", TokenType.BYTE);
		expectToken("Parsing byte failed", "03945b", TokenType.BYTE);
		expectToken("Parsing byte failed", "+3545b", TokenType.BYTE);
	}

	@Test
	public void testIntLiteral() throws Exception {
		expectToken("Parsing int failed", "0", TokenType.INT);
		expectToken("Parsing int failed", "-3435", TokenType.INT);
		expectToken("Parsing int failed", "039415", TokenType.INT);
		expectToken("Parsing int failed", "+3545", TokenType.INT);
	}

	@Test
	public void testFloatLiteral() throws Exception {
		expectToken("Parsing float failed", "0.", TokenType.FLOAT);
		expectToken("Parsing float failed", "0.567", TokenType.FLOAT);
		expectToken("Parsing float failed", "-0.", TokenType.FLOAT);
		expectToken("Parsing float failed", "-4560.456", TokenType.FLOAT);
		expectToken("Parsing float failed", "+5460.9", TokenType.FLOAT);
		expectToken("Parsing float failed", "+.", TokenType.FLOAT);
		expectToken("Parsing float failed", ".", TokenType.FLOAT);
	}

	@Test
	public void testStringLiteral() throws Exception {
		expectToken("Parsing string failed", "\"abc \"", TokenType.STRING);
		expectToken("Parsing string failed", "\"\\\" \"", TokenType.STRING);
		expectToken("Parsing string failed", "\"\\\t \"", TokenType.STRING);
		expectToken("Parsing string failed", "\" sdfkdfgl \\\" \"", TokenType.STRING);
		expectToken("Parsing string failed", "\"\"", TokenType.STRING);
		expectToken("Parsing string failed", "\"\\\"\\\"\"", TokenType.STRING);
	}

	@Test
	public void testNameLiteral() throws Exception {
		expectToken("Parsing name failed", "aAz435a", TokenType.NAME);
		expectToken("Parsing name failed", "asA_34", TokenType.NAME);
		expectToken("Parsing name failed", "A", TokenType.NAME);
		expectToken("Parsing name failed", "Asdf!", TokenType.NAME);
		expectToken("Parsing name failed", "voida", TokenType.NAME);
		expectToken("Parsing name failed", "int_", TokenType.NAME);
	}

	@Test
	public void testTypeLiteral() throws Exception {
		expectToken("Parsing type literal failed", "bool", TokenType.TYPE);
		expectToken("Parsing type literal failed", "byte", TokenType.TYPE);
		expectToken("Parsing type literal failed", "int", TokenType.TYPE);
		expectToken("Parsing type literal failed", "float", TokenType.TYPE);
		expectToken("Parsing type literal failed", "pointer", TokenType.TYPE);
		expectToken("Parsing type literal failed", "string", TokenType.TYPE);
	}

	@Test
	public void testVoidLiteral() throws Exception {
		expectToken("Parsing void literal failed", "void", TokenType.VOID);
	}

	@Test
	public void testComment() throws Exception {
		expectToken("Parsing comment failed", "#", TokenType.COMMENT);
		expectToken("Parsing comment failed", "#s sdghf sdj!dfp", TokenType.COMMENT);
	}

	@Test
	public void testOtherLiterals() throws Exception {
		expectToken("Parsing left parantheses failed", "(", TokenType.LEFT_PARANTHESES);
		expectToken("Parsing right parantheses failed", ")", TokenType.RIGHT_PARANTHESES);
		expectToken("Parsing comma failed", ",", TokenType.COMMA);
		expectToken("Parsing line break failed", "\n", TokenType.LINE_BREAK);
		expectToken("Parsing equal sign failed", "=", TokenType.EQUAL_SIGN);
		expectToken("Parsing end of file failed", "", TokenType.EOF);
	}

	@Test
	public void testFunctionCall() throws Exception {
		setInput("test_t \"Hallo\", 34.3, -4b, \"Hallo \\\"\", true, .\n");
		expectNextToken("Parsing name literal failed", TokenType.NAME, "test_t");
		expectNextToken("Parsing string literal failed", TokenType.STRING, "\"Hallo\"");
		expectNextToken("Parsing comma failed", TokenType.COMMA);
		expectNextToken("Parsing float literal failed", TokenType.FLOAT, "34.3");
		expectNextToken("Parsing comma failed", TokenType.COMMA);
		expectNextToken("Parsing byte literal failed", TokenType.BYTE, "-4b");
		expectNextToken("Parsing comma failed", TokenType.COMMA);
		expectNextToken("Parsing string literal failed", TokenType.STRING, "\"Hallo \\\"\"");
		expectNextToken("Parsing comma failed", TokenType.COMMA);
		expectNextToken("Parsing bool literal failed", TokenType.BOOL, "true");
		expectNextToken("Parsing comma failed", TokenType.COMMA);
		expectNextToken("Parsing short float literal failed", TokenType.FLOAT, ".");
		expectNextToken("Parsing line break failed", TokenType.LINE_BREAK);
		expectNextToken("Parsing end of file failed", TokenType.EOF);
	}

    @Test
    public void testFunctionComment() throws Exception {
        setInput("#\n_function");
		expectNextToken("Parsing comment failed", TokenType.COMMENT, "#");
		expectNextToken("Parsing line break failed", TokenType.LINE_BREAK);
		expectNextToken("Parsing function keyword failed", TokenType.FUNCTION_KEYWORD, "_function");
		expectNextToken("Parsing end of file failed", TokenType.EOF);
    }

    @Test
    public void testEndKeyword() throws Exception {
        setInput("_end\n");
		expectNextToken("Parsing end keyword failed", TokenType.END_KEYWORD, "_end");
		expectNextToken("Parsing line break failed", TokenType.LINE_BREAK);
		expectNextToken("Parsing end of file failed", TokenType.EOF);
    }

	@Test
	public void testIgnoreWhitespace() throws Exception {
		expectToken("Failed to ignore whitespace", "\t abcd  ", TokenType.NAME, "abcd");
		expectToken("Failed to ignore whitespace", "  \t .  ", TokenType.FLOAT, ".");
		expectToken("Failed to ignore whitespace", "    abcd  \r", TokenType.NAME, "abcd");
		expectToken("Failed to ignore whitespace", "   \r  \n  \t", TokenType.LINE_BREAK, "\n");
	}

	private void setInput(String str){
		lexer = new Lexer(new ByteArrayInputStream(str.getBytes()));
	}

	private void expectNextToken(String message, TokenType expectedType) throws Exception {
		Token token = lexer.nextToken();
		assertEquals(message + ", type", expectedType, token.type);
	}

	private void expectNextToken(String message, TokenType expectedType, String expectedText) throws Exception {
		Token token = lexer.nextToken();
		assertEquals(message + ", type", expectedType, token.type);
		assertEquals(message + ", text", expectedText, token.text);
	}

	/**
	 * Test parsing input for one token.
	 */
	private void expectToken(String message, String input, TokenType expectedType) throws Exception {
		expectToken(message, input, expectedType, input);
	}

	/**
	 * Test parsing input for one token.
	*/
	private void expectToken(String message, String input, TokenType expectedType, String expectedText) throws Exception {
		setInput(input);
		Token token = lexer.nextToken();
		assertEquals(message + ", type", expectedType, token.type);
		assertEquals(message + ", text", expectedText, token.text);
		assertEquals(message + ", more tokens than expected", TokenType.EOF.toString(), lexer.nextToken().type.toString());
	}
}