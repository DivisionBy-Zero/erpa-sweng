package ch.epfl.sweng.erpa.services.GCP;

import com.annimon.stream.Optional;

import lombok.Getter;

public class ServerException extends Exception {
    @Getter private final Optional<Integer> errorCode;

    private ServerException(String message, Optional<Integer> errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServerException() {
        this("Server returned an error", Optional.empty());
    }

    public ServerException(int errCode) {
        this("Server returned HTTP Error " + errCode, Optional.of(errCode));
    }

    public ServerException(int errCode, String errMessage) {
        this(getMessage(errCode, errMessage), Optional.of(errCode));
    }

    private static String getMessage(int errCode, String errMessage) {
        String message = "Server returned HTTP Error " + errCode + ", with error message: \n";
        if (errMessage == null || errMessage.isEmpty()) {
            message += "(empty)";
        } else {
            message += errMessage;
        }
        return message;
    }
}
