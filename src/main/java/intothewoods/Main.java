package intothewoods;

import intothewoods.lexer.BasicLexer;
import intothewoods.lexer.LexerException;
import intothewoods.lexer.Token;
import intothewoods.lexer.TokenType;
//import intothewoods.parser.BasicParser;
//import intothewoods.parser.SyntaxException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
	    //Some testing code
		/*BasicParser parser = new BasicParser();
	    List<String> stringList = new ArrayList<>();
	    //stringList.add("_function name void int");
	    stringList.add("abc = 39b");
	    //stringList.add("_end");

	    try {
		    List<BasicParser.Line> lines = parser.parseLines(stringList);
		    for (BasicParser.Line line : lines) {
			    System.out.println(line);
		    }
	    } catch (SyntaxException e) {
		    e.printStackTrace();
	    }*/
	    BasicLexer lexer = new BasicLexer(new ByteArrayInputStream(".".getBytes()));
	    Token token;
	    do {
		    try {
			    token = lexer.nextToken();
		    } catch (IOException | LexerException ex){
			    System.err.println(ex);
			    break;
		    }
		    System.out.println(token);
	    } while(token.type != TokenType.EOF);
    }
}
