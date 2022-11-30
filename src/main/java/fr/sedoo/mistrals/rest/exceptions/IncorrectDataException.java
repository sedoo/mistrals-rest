package fr.sedoo.mistrals.rest.exceptions;

public class IncorrectDataException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	public IncorrectDataException(String message) {
		super(message);
	}

}
