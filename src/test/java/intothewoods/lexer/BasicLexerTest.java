package intothewoods.lexer;

import intothewoods.common.TokenType;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Test the main lexer implementation.
 */
public class BasicLexerTest {

	private BasicLexer lexer;

	@Test
	public void testBoolLiteral() throws Exception {
		expectToken("Lexing true failed", "true", TokenType.BOOL_LITERAL);
		expectToken("Lexing false failed", "false", TokenType.BOOL_LITERAL);
	}

	@Test
	public void testByteLiteral() throws Exception {
		expectToken("Lexing byte failed", "0b", TokenType.BYTE_LITERAL);
		expectToken("Lexing byte failed", "-3435b", TokenType.BYTE_LITERAL);
		expectToken("Lexing byte failed", "03945b", TokenType.BYTE_LITERAL);
		expectToken("Lexing byte failed", "+3545b", TokenType.BYTE_LITERAL);
	}

	@Test
	public void testIntLiteral() throws Exception {
		expectToken("Lexing int failed", "0", TokenType.INT_LITERAL);
		expectToken("Lexing int failed", "-3435", TokenType.INT_LITERAL);
		expectToken("Lexing int failed", "039415", TokenType.INT_LITERAL);
		expectToken("Lexing int failed", "+3545", TokenType.INT_LITERAL);
	}

	@Test
	public void testFloatLiteral() throws Exception {
		expectToken("Lexing float failed", "0.0", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "0.567", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "-0.0", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "-4560.456", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "+5460.9", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "+08.09", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "+06E-90", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "+06E+90", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "-06E-0", TokenType.FLOAT_LITERAL);
		expectToken("Lexing float failed", "06E+0", TokenType.FLOAT_LITERAL);
	}

	@Test
	public void testStringLiteral() throws Exception {
		expectToken("Lexing string failed", "\"abc \"", TokenType.STRING_LITERAL);
		expectToken("Lexing string failed", "\"\\\" \"", TokenType.STRING_LITERAL);
		expectToken("Lexing string failed", "\" sdfkdfgl \\\" \"", TokenType.STRING_LITERAL);
		expectToken("Lexing string failed", "\"\"", TokenType.STRING_LITERAL);
		expectToken("Lexing string failed", "\"\\\"\\\"\"", TokenType.STRING_LITERAL);
	}

	@Test
	public void testNameLiteral() throws Exception {
		expectToken("Lexing name failed", "aAz435a", TokenType.NAME);
		expectToken("Lexing name failed", "asA_34", TokenType.NAME);
		expectToken("Lexing name failed", "A", TokenType.NAME);
		expectToken("Lexing name failed", "Asdf!", TokenType.NAME);
		expectToken("Lexing name failed", "voida", TokenType.NAME);
		expectToken("Lexing name failed", "int_", TokenType.NAME);
	}

	@Test
	public void testTypeLiteral() throws Exception {
		expectToken("Lexing type literal failed", "bool", TokenType.TYPE);
		expectToken("Lexing type literal failed", "byte", TokenType.TYPE);
		expectToken("Lexing type literal failed", "int", TokenType.TYPE);
		expectToken("Lexing type literal failed", "float", TokenType.TYPE);
		expectToken("Lexing type literal failed", "pointer", TokenType.TYPE);
		expectToken("Lexing type literal failed", "string", TokenType.TYPE);
	}

	@Test
	public void testVoidLiteral() throws Exception {
		expectToken("Lexing void literal failed", "void", TokenType.VOID);
	}

	@Test
	public void testComment() throws Exception {
		expectToken("Lexing comment failed", "#", TokenType.COMMENT);
		expectToken("Lexing comment failed", "#s sdghf sdj!dfp", TokenType.COMMENT);
	}

	@Test
	public void testOtherLiterals() throws Exception {
		expectToken("Lexing left parantheses failed", "(", TokenType.LEFT_PARENTHESIS);
		expectToken("Lexing right parantheses failed", ")", TokenType.RIGHT_PARENTHESIS);
		expectToken("Lexing comma failed", ",", TokenType.COMMA);
		expectToken("Lexing line break failed", "\n", TokenType.NEW_LINE);
		expectToken("Lexing equal sign failed", "=", TokenType.EQUAL_SIGN);
		expectToken("Lexing end of file failed", "", TokenType.EOF);
	}

	@Test
	public void testFunctionCall() throws Exception {
		setInput("test_t \"Hallo\", 34.3, -4b, \"Hallo \\\"\", true, 0.0\n");
		expectNextToken("Lexing name literal failed", TokenType.NAME, "test_t");
		expectNextToken("Lexing string literal failed", TokenType.STRING_LITERAL, "\"Hallo\"");
		expectNextToken("Lexing comma failed", TokenType.COMMA);
		expectNextToken("Lexing float literal failed", TokenType.FLOAT_LITERAL, "34.3");
		expectNextToken("Lexing comma failed", TokenType.COMMA);
		expectNextToken("Lexing byte literal failed", TokenType.BYTE_LITERAL, "-4b");
		expectNextToken("Lexing comma failed", TokenType.COMMA);
		expectNextToken("Lexing string literal failed", TokenType.STRING_LITERAL, "\"Hallo \\\"\"");
		expectNextToken("Lexing comma failed", TokenType.COMMA);
		expectNextToken("Lexing bool literal failed", TokenType.BOOL_LITERAL, "true");
		expectNextToken("Lexing comma failed", TokenType.COMMA);
		expectNextToken("Lexing short float literal failed", TokenType.FLOAT_LITERAL, "0.0");
		expectNextToken("Lexing line break failed", TokenType.NEW_LINE);
		expectNextToken("Lexing end of file failed", TokenType.EOF);
	}

    @Test
    public void testFunctionComment() throws Exception {
        setInput("#\n_function");
		expectNextToken("Lexing comment failed", TokenType.COMMENT, "#");
		expectNextToken("Lexing line break failed", TokenType.NEW_LINE);
		expectNextToken("Lexing function keyword failed", TokenType.FUNCTION_KEYWORD, "_function");
		expectNextToken("Lexing end of file failed", TokenType.EOF);
    }

    @Test
    public void testEndKeyword() throws Exception {
        setInput("_end\n");
		expectNextToken("Lexing end keyword failed", TokenType.END_KEYWORD, "_end");
		expectNextToken("Lexing line break failed", TokenType.NEW_LINE);
		expectNextToken("Lexing end of file failed", TokenType.EOF);
    }

	@Test
	public void testIgnoreWhitespace() throws Exception {
		expectToken("Failed to ignore whitespace", "\t abcd  ", TokenType.NAME, "abcd");
		expectToken("Failed to ignore whitespace", "  \t 0.0  ", TokenType.FLOAT_LITERAL, "0.0");
		expectToken("Failed to ignore whitespace", "    abcd  \r", TokenType.NAME, "abcd");
		expectToken("Failed to ignore whitespace", "   \r  \n  \t", TokenType.NEW_LINE, "\n");
	}

	@Test(expected = LexerException.class)
	public void testInvalidStringLiteral() throws Exception {
		runToEndOnInput("\"ABCD\"\"");
		runToEndOnInput("\"ABCD\n\"");
	}

	@Test(expected = LexerException.class)
	public void testInvalidStringLiteral2() throws Exception {
		runToEndOnInput("\"\\\n\"");
	}

	@Test(expected = LexerException.class)
	public void testInvalidStringLiteral3() throws Exception {
		runToEndOnInput("\"\\");
	}

	@Test(expected = LexerException.class)
	public void testInvalidIntLiteral() throws Exception {
		runToEndOnInput("--0");
		runToEndOnInput("+");
	}

	@Test(expected = LexerException.class)
	public void testInvalidFloatLiteral() throws Exception {
		runToEndOnInput("+.b");
		runToEndOnInput("+-0.0");
		runToEndOnInput(".0");
		runToEndOnInput(".");
		runToEndOnInput("0E");
	}

	private void runToEndOnInput(String str) throws Exception {
		setInput(str);
		while (lexer.getToken().getType() != TokenType.EOF){
			lexer.nextToken();
		}
	}

	private void setInput(String str) throws Exception {
		lexer = new BasicLexer(new ByteArrayInputStream(str.getBytes()));
	}

	private void expectNextToken(String message, TokenType expectedType) throws Exception {
		LexerToken token = lexer.getToken();
		assertEquals(message + ", type", expectedType, token.getType());
		lexer.nextToken();
	}

	private void expectNextToken(String message, TokenType expectedType, String expectedText) throws Exception {
		LexerToken token = lexer.getToken();
		assertEquals(message + ", type", expectedType, token.getType());
		assertEquals(message + ", text", expectedText, token.getText());
		lexer.nextToken();
	}

	/**
	 * Test lexing input for one token.
	 */
	private void expectToken(String message, String input, TokenType expectedType) throws Exception {
		expectToken(message, input, expectedType, input);
	}

	/**
	 * Test lexing input for one token.
	*/
	private void expectToken(String message, String input, TokenType expectedType, String expectedText) throws Exception {
		setInput(input);
		LexerToken token = lexer.getToken();
		assertEquals(message + ", type", expectedType, token.getType());
		assertEquals(message + ", text", expectedText, token.getText());
		assertEquals(message + ", more tokens than expected", TokenType.EOF.toString(), lexer.nextToken().getType().toString());
	}
}