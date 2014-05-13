package intothewoods;

import intothewoods.lexer.Lexer;
import intothewoods.lexer.Token;
import intothewoods.lexer.TokenType;
//import intothewoods.parser.BasicParser;
//import intothewoods.parser.SyntaxException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
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
	    Lexer lexer = new Lexer(new ByteArrayInputStream(".".getBytes()));
	    Token token;
	    do {
		    try {
			    token = lexer.nextToken();
		    } catch (IOException | intothewoods.lexer.SyntaxException ex){
			    System.err.println(ex);
			    break;
		    }
		    System.out.println(token);
	    } while(token.type != TokenType.EOF);
    }
}
