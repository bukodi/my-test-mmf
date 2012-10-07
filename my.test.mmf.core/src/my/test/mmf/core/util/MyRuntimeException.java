package my.test.mmf.core.util;

public class MyRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -1291476678339471253L;

	public MyRuntimeException() {
	}

	public MyRuntimeException(String message) {
		super(message);
	}

	public MyRuntimeException(Throwable cause) {
		super(cause);
	}

	public MyRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyRuntimeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
