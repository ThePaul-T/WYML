package net.creeperhost.mutliblockapi;

public class MultiblockValidationException extends Exception
{
	private static final long serialVersionUID = -4038176177468678877L;

	public MultiblockValidationException(String reason) {
		super(reason);
	}
}
