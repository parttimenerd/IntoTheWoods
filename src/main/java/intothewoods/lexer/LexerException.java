package intothewoods.lexer;

/**
 * Exception raised when the lexer spots a syntax error.
 */
public class LexerException extends Exception {

    private final int line;
	private final int column;

    public LexerException(String message, int line, int column, String currentCodeLine){
        super(composeMessage(message, line, column, currentCodeLine));
        this.line = line;
	    this.column = column;
    }

    public int getLine(){
        return line;
    }

	public int getColumn(){
		return column;
	}

	public static String composeMessage(String message, int line, int column, String currentCodeLine){
		String str = "Error at " + line + "[" + column + "]: ";
		str += currentCodeLine.substring(0, column);
		str += "¦" + message + "¦" + currentCodeLine.substring(column);
		return str;
	}
}
