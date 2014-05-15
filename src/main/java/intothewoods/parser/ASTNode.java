package intothewoods.parser;

import intothewoods.common.TokenType;
import intothewoods.common.Token;
import intothewoods.lexer.LexerToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple homogenous AST (Abstract Syntax Tree).
 */
public class ASTNode {

	private Token token;
	private List<ASTNode> children;

	/**
	 * Empty contructor for making nil rooted trees.
	 */
	public ASTNode(){
		this.token = new Token(TokenType.NIL);
	}

	/**
	 * Initialize an ASTNode with a given token,
	 * The type of this ASTNode is the type of the token.
	 * @param token given token
	 */
	public ASTNode(Token token){
		this.token = token;
	}

	/**
	 * Initialize an imaginary node with the given type.
	 * @param type given type
	 */
	public ASTNode(TokenType type){
		this.token = new Token(type);
	}

	/**
	 * Return the type of this node.
	 * @return type of this node
	 */
	public TokenType getType(){
		return token.getType();
	}

	/**
	 * Checks whether or not this node has the given type.
	 * @param type given type
	 * @return does this node have the given type?
	 */
	public boolean hasType(TokenType type){
		return token.getType() == type;
	}

	/**
	 * Add the given child to this node.
	 * @param newChild given child
	 */
	public void addChild(ASTNode newChild){
		if (children == null){
			children = new ArrayList<>();
		}
		children.add(newChild);
	}

	/**
	 * Add the given token as a child to this node.
	 * @param newChild given child
	 */
	public void addChild(Token newChild){
		if (children == null){
			children = new ArrayList<>();
		}
		children.add(new ASTNode(newChild));
	}

	/**
	 * Checks whether or not this node is a leaf node (e.g. has no children).
	 * @return is this a leaf node.
	 */
	public boolean isLeaf(){
		return children == null || children.isEmpty();
	}

	/**
	 * Returns the number of children.
	 * @return number of children.
	 */
	public int getNumberOfChildren(){
		if (children == null){
			return 0;
		}
		return children.size();
	}

	/**
	 * Get the child at the given child index.
	 *
	 * @param index given child index
	 * @return child at given index
	 */
	public ASTNode getChild(int index){
		if (children == null){
			children = new ArrayList<>();
		}
		return children.get(index);
	}

	@Override
	public String toString(){
		return token.toString();
	}

	/**
	 * Stringify the tree represented by this node.
	 * @return stringified tree
	 */
	public String toStringTree(){
		if (isLeaf()){
			return toString();
		}
		StringBuilder builder = new StringBuilder();
		if (!hasType(TokenType.NIL)){
			builder.append(')');
			builder.append(toString());
			builder.append(' ');
		}
		for (int i = 0; i < children.size(); i++){
			if (i > 0){
				builder.append(' ');
			}
			builder.append(children.get(i).toStringTree());
		}
		if (!hasType(TokenType.NIL)){
			builder.append(')');
		}
		return builder.toString();
	}
}