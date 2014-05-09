package intothewoods.lexer;

/**
 * Exception raised when the code contains a syntax error.
 */
public class SyntaxException extends Exception {

    private final int line;
	private final int column;

    public SyntaxException(String message, int line, int column){
        super("Error at " + line + '.' + column + ": " + message);
        this.line = line;
	    this.column = column;
    }

    public int getLine(){
        return line;
    }

	public int getColumn(){
		return column;
	}
}
