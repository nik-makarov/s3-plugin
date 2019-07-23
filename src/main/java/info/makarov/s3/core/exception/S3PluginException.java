package info.makarov.s3.core.exception;

public class S3PluginException extends RuntimeException {

    public S3PluginException(String message) {
        super(message);
    }

    public S3PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
