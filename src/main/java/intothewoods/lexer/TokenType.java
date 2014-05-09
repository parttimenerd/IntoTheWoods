package intothewoods.lexer;

/**
 * Type of a token.
 * @see Language specification
 */
public enum TokenType {

	NONE,
	BOOL,
	BYTE,
	INT,
	FLOAT,
	STRING,
	NAME,
	TYPE,
	VOID,
	FUNCTION_KEYWORD,
	RETURN_KEYWORD,
	IF_KEYWORD,
	ELSE_KEYWORD,
	END_KEYWORD,
	WHILE_KEYWORD,
	COMMENT,
	LEFT_PARANTHESES,
	RIGHT_PARANTHESES,
	COMMA,
	LINE_BREAK,
	EQUAL_SIGN,
	EOF

}
