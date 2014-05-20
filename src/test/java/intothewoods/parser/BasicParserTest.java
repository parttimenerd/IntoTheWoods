package intothewoods.parser;

import intothewoods.common.TokenType;
import intothewoods.lexer.BasicLexer;
import intothewoods.lexer.LexerToken;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

/**
 * Tests the BasicParser.
 *
 * It assumes that the BasicLexer and the ASTNode class work correctly.
 */
public class BasicParserTest {

	private BasicParser parser;

	@Test
	public void testWholeFileParsing() throws Exception {
		setInput("int abc = 3\n_function void main\n_end\nint abcd = 4");
		ASTNode node = parser.parseTokens();
		assertEquals("Parsing whole file failed", TokenType.WHOLE_FILE, node.getType());
		assertEquals("Parsing whole file failed, number of children", 2, node.getNumberOfChildren());
		ASTNode globalNode = node.getChild(0);
		assertEquals("Parsing whole file failed", TokenType.GLOBALS, globalNode.getType());
		assertEquals("Parsing whole file failed", 2, globalNode.getNumberOfChildren());
		assertEquals("Parsing whole file failed, globals declaration",
				TokenType.VARIABLE_DECLARATION, globalNode.getChild(0).getType());
		ASTNode funcNode = node.getChild(1);
		assertEquals("Parsing whole file failed, functions", TokenType.FUNCTIONS, funcNode.getType());
		assertEquals("Parsing whole file failed, functions", 1, funcNode.getNumberOfChildren());
		assertEquals("Parsing whole file failed, functions",
				TokenType.FUNCTION_DECLARATION, funcNode.getChild(0).getType());
	}

	@Test
	public void testCurrentLineParsing() throws Exception {
		assertCurrentLineTypeEquals("Parsing line failed", "_if abc\n_end", TokenType.CONDITION);
		assertCurrentLineTypeEquals("Parsing line failed", "_while abc\n_end", TokenType.LOOP);
		assertCurrentLineTypeEquals("Parsing line failed", "abc = a", TokenType.VARIABLE_ASSIGNMENT);
		assertCurrentLineTypeEquals("Parsing line failed", "abc 3", TokenType.FUNCTION_CALL);
		assertCurrentLineTypeEquals("Parsing line failed", "abc", TokenType.FUNCTION_CALL);
		assertCurrentLineTypeEquals("Parsing line failed", "int abc = 3", TokenType.VARIABLE_DECLARATION);
		assertCurrentLineTypeEquals("Parsing line failed", "int abc = q", TokenType.VARIABLE_DECLARATION);
		assertCurrentLineTypeEquals("Parsing line failed", "_return", TokenType.RETURN_STATEMENT);
		assertCurrentLineTypeEquals("Parsing line failed", "_return 3b", TokenType.RETURN_STATEMENT);
		assertCurrentLineTypeEquals("Parsing line failed", "_return ab", TokenType.RETURN_STATEMENT);
		assertCurrentLineTypeEquals("Parsing line failed", "#abcd", TokenType.COMMENT);
	}

	private void assertCurrentLineTypeEquals(String message, String input, TokenType expectedType) throws Exception {
		setInput(input);
		ASTNode node = parser.parseCurrentLine();
		assertEquals(message, expectedType, node.getType());
	}

	@Test
	public void testVariableDeclarationParsing() throws Exception {
		setInput("int val = name");
		ASTNode node = parser.parseVariableDeclaration();
		String msg = "Parsing variable declaration failed";
		assertEquals(msg + ", type", TokenType.VARIABLE_DECLARATION, node.getType());
		assertEquals(msg + ", type node", TokenType.TYPE, node.getChild(0).getType());
		assertEquals(msg + ", name node", TokenType.NAME, node.getChild(1).getType());
		assertEquals(msg + ", value node", TokenType.VALUE, node.getChild(2).getType());
		assertEquals(msg + ", number of node", 3, node.getNumberOfChildren());
		assertEquals(msg + ", value node child node", TokenType.NAME, node.getChild(2).getChild(0).getType());
	}

	@Test
	public void testAssignmentParsing() throws Exception {
		setInput("a = b");
		ASTNode node = parser.parseVariableAssignment();
		assertEquals("Parsing assignment failed, type of first", TokenType.NAME, node.getChild(0).getType());
		assertEquals("Parsing assignment failed, type of second", TokenType.VALUE, node.getChild(1).getType());
		assertEquals("Parsing assignment failed, node has more than two children", 2, node.getNumberOfChildren());
	}

	@Test
	public void testFunctionDeclarationParsing() throws Exception {
		assertFunctionDeclarationEquals("_function void abc\nabc\nabcd\n_end",
				TokenType.FUNCTION_CALL, TokenType.FUNCTION_CALL);
		assertFunctionDeclarationEquals("_function void abc\n_end");
	}

	private void assertFunctionDeclarationEquals(String input, TokenType... expectedBockNodeTypes) throws Exception {
		setInput(input);
		ASTNode funcNode = parser.parseFunctionDeclaration();
		assertEquals("Parsing function declaration failed", TokenType.FUNCTION_DECLARATION, funcNode.getType());
		assertEquals("Parsing function declaration failed, number of child nodes", 2, funcNode.getNumberOfChildren());
		assertEquals("Parsing function declaration failed, function header",
				TokenType.FUNCTION_HEADER, funcNode.getChild(0).getType());
		ASTNode codeBlock = funcNode.getChild(1);
		assertCodeBlockEquals("Parsing function declaration failed", codeBlock, expectedBockNodeTypes);
	}

	private void assertCodeBlockEquals(String message, ASTNode actualCodeBlockNode, TokenType... expectedNodeTypes){
		assertEquals(message + ", code block", TokenType.CODE_BLOCK, actualCodeBlockNode.getType());
		assertEquals(message + ", number of code block statements",
				expectedNodeTypes.length, actualCodeBlockNode.getNumberOfChildren());
		for (int i = 0; i < expectedNodeTypes.length; i++) {
			assertEquals(message + ", types of code block statements",
					expectedNodeTypes[i], actualCodeBlockNode.getChild(i).getType());
		}
	}

	@Test
	public void testFunctionHeaderParsing() throws Exception {
		assertFunctionHeader("_function void abc",
				"(FUNCTION_HEADER void abc)");
		assertFunctionHeader("_function void abc : int abcd",
				"(FUNCTION_HEADER void abc (PARAMETER_DECL_LIST (PARAMETER_DECL int abcd)))");
		assertFunctionHeader("_function void abc : int abcd, float var",
				"(FUNCTION_HEADER void abc " +
						"(PARAMETER_DECL_LIST (PARAMETER_DECL int abcd) (PARAMETER_DECL float var)))"
		);
	}

	private void assertFunctionHeader(String input, String expectedNodeTree) throws Exception {
		setInput(input);
		ASTNode headerNode = parser.parseFunctionHeader();
		assertTreeEquals("Parsing function header failed", expectedNodeTree, headerNode);
	}

	@Test
	public void testFunctionCallParsing() throws Exception {
		assertFunctionCallEquals("abc", "(FUNCTION_CALL abc)");
		assertFunctionCallEquals("abc name", "(FUNCTION_CALL abc (VALUE name))");
		assertFunctionCallEquals("abc 3 name \"ich\"", "(FUNCTION_CALL abc (VALUE 3) (VALUE name) (VALUE \"ich\"))");
	}

	private void assertFunctionCallEquals(String input, String expectedNodeTree) throws Exception {
		setInput(input);
		assertTreeEquals("Parsing function call failed", expectedNodeTree, parser.parseFunctionCall());
	}

	@Test
	public void testReturnStatementParsing() throws Exception {
		assertReturnStatementEquals("_return 3", "(RETURN_STATEMENT (VALUE 3))");
		assertReturnStatementEquals("_return", "RETURN_STATEMENT");
	}

	private void assertReturnStatementEquals(String input, String expectedNodeTree) throws Exception {
		setInput(input);
		assertTreeEquals("Parsing return statement failed", expectedNodeTree, parser.parseReturnStatement());
	}

	@Test
	public void testLoopParsing() throws Exception {
		assertLoopEquals("_while abc\n_end");
		assertLoopEquals("_while \"hey\"\n	abcd 3 3\n  _end", TokenType.FUNCTION_CALL);
		assertLoopEquals("_while \"hey\"\n_while 3\n_end\n_return 2\n  _end", TokenType.LOOP, TokenType.RETURN_STATEMENT);
	}

	private void assertLoopEquals(String input, TokenType... expectedBockNodeTypes) throws Exception {
		setInput(input);
		ASTNode loopNode = parser.parseLoop();
		assertEquals("Parsing loop failed", TokenType.LOOP, loopNode.getType());
		assertEquals("Parsing loop failed, number of child nodes", 2, loopNode.getNumberOfChildren());
		assertEquals("Parsing loop parameter failed", TokenType.VALUE, loopNode.getChild(0).getType());
		ASTNode codeBlock = loopNode.getChild(1);
		assertCodeBlockEquals("Parsing loop failed", codeBlock, expectedBockNodeTypes);
	}

	@Test
	public void testConditionParsing() throws Exception {
		assertConditionWOElseEquals("_if 3\n_end");
		assertConditionWOElseEquals("_if name \n hey 3\n _end", TokenType.FUNCTION_CALL);
		assertConditionWElseEquals("_if true \n_else\n _end\n", new TokenType[0], new TokenType[0]);
		TokenType[] arr = new TokenType[1];
		arr[0] = TokenType.FUNCTION_CALL;
		assertConditionWElseEquals("_if true \nanswer 34\n_else\nhey 3 4 \n_end", arr, arr);
	}

	private void assertConditionWOElseEquals(String input, TokenType... expectedBockNodeTypes) throws Exception {
		setInput(input);
		ASTNode condNode = parser.parseCondition();
		assertEquals("Parsing condition failed", TokenType.CONDITION, condNode.getType());
		assertEquals("Parsing condition failed, number of child nodes", 2, condNode.getNumberOfChildren());
		assertEquals("Parsing condition parameter failed", TokenType.VALUE, condNode.getChild(0).getType());
		ASTNode codeBlock = condNode.getChild(1);
		assertCodeBlockEquals("Parsing condition failed", codeBlock, expectedBockNodeTypes);
	}

	private void assertConditionWElseEquals(String input, TokenType[] expectedBockNodeTypes,
											TokenType[] expectedElseBockNodeTypes) throws Exception {
		setInput(input);
		ASTNode condNode = parser.parseCondition();
		assertEquals("Parsing condition failed", TokenType.CONDITION, condNode.getType());
		assertEquals("Parsing condition failed, number of child nodes", 3, condNode.getNumberOfChildren());
		assertEquals("Parsing condition parameter failed", TokenType.VALUE, condNode.getChild(0).getType());
		assertCodeBlockEquals("Parsing condition failed", condNode.getChild(1), expectedBockNodeTypes);
		assertCodeBlockEquals("Parsing condition failed", condNode.getChild(2), expectedElseBockNodeTypes);
	}

	@Test
	public void testNestedConditionsAndLoops() throws Exception {
		String msg = "Parsing nested conditions and loops failed";
		setInput("_if abc\n" +
				"	_return\n" +
				"	_while true \n" +
				"		_if 4\n" +
				"			answer 42 \n" +
				"		_else \n" +
				"			hey 3 4\n" +
				"			string me = \"Jo...\"\n" +
				"		_end \n" +
				"	_end\n" +
				"_end");
		ASTNode node = parser.parseCondition();
		assertTreeEquals(msg, "(CONDITION (VALUE abc) (CODE_BLOCK " +
				"RETURN_STATEMENT " +
				"(LOOP (VALUE true) (CODE_BLOCK " +
				"(CONDITION (VALUE 4) (CODE_BLOCK " +
				"(FUNCTION_CALL answer (VALUE 42))) " +
				"(CODE_BLOCK " +
				"(FUNCTION_CALL hey (VALUE 3) (VALUE 4)) " +
				"(VARIABLE_DECLARATION string me (VALUE \"Jo...\"))" +
				"))))))", node);
	}

	@Test
	public void testLiteralParsing() throws Exception {
		assertParseLiteralEquals("3");
		assertParseLiteralEquals("-3456");
		assertParseLiteralEquals("\"absdf\t\\\"\"");
		assertParseLiteralEquals("true");
		assertParseLiteralEquals("3.7");
		assertParseLiteralEquals("0b");
	}

	private void assertParseLiteralEquals(String input) throws Exception {
		BasicLexer lexer = new BasicLexer(new ByteArrayInputStream(input.getBytes()));
		LexerToken token = lexer.getToken();
		setInput("Bla");
		assertTreeEquals("Parsing literal failed", "(VALUE " + input + ')', parser.parseTokenAsValue(token));
	}

    @Test
    public void testNameValueParsing() throws Exception {
		BasicLexer lexer = new BasicLexer(new ByteArrayInputStream("name".getBytes()));
		setInput("Bla");
		LexerToken token = lexer.getToken();
		ASTNode node = parser.parseTokenAsValue(token);
		assertEquals("Parsing name as value failed", TokenType.VALUE, node.getType());
		node = node.getChild(0);
		assertEquals("Parsing name value failed, type", TokenType.NAME, node.getType());
		assertEquals("Parsing name value failed, text", "name", node.getText());
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader1() throws Exception {
        setInput("_function void main : void q\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader2() throws Exception {
        setInput("_function void main e int q\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader3() throws Exception {
        setInput("_function void main :\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader4() throws Exception {
        setInput("_function void main : int 2\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader5() throws Exception {
        setInput("_function void main : int q bool w\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader6() throws Exception {
        setInput("_function void main : int q, bool w ,\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader7() throws Exception {
        setInput("_function void main : int q error int w\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader8() throws Exception {
        setInput("_function qwxs main\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testIllegalFunctionHeader9() throws Exception {
        setInput("_function int 23\n");
        parser.parseFunctionHeader();
    }

    @Test(expected = ParserException.class)
    public void testNoReturnStatement() throws Exception {
        setInput("_function int main\n _end");
        parser.parseFunctionDeclaration();
    }

    @Test(expected = ParserException.class)
    public void testIfWithoutEnd() throws Exception {
        setInput("_if true");
        parser.parseCondition();
    }

    @Test(expected = ParserException.class)
    public void testIllegalIfCondition() throws Exception {
        setInput("_if _if\n_end");
        parser.parseCondition();
    }

    @Test(expected = ParserException.class)
    public void testWhileWithoutEnd() throws Exception {
        setInput("_while true");
        parser.parseLoop();
    }

    @Test(expected = ParserException.class)
    public void testIllegalWhileCondition() throws Exception {
        setInput("_while _if\n_end");
        parser.parseLoop();
    }

    private void setInput(String input) throws Exception {
		BasicLexer lexer = new BasicLexer(new ByteArrayInputStream(input.getBytes()));
		parser = new BasicParser(lexer);
	}

	private void assertTreeEquals(String message, String expectedTree, ASTNode actualTree) throws Exception {
		assertEquals(message, expectedTree, actualTree.toStringTree());
	}
}