package models.tcp;

import enums.ResponseStatus;
import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private ResponseStatus status;
    private String message;
    private Object data;

    public Response() {
    }

    public Response(ResponseStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Response ok(String message, Object data) {
        return new Response(ResponseStatus.OK, message, data);
    }

    public static Response error(String message) {
        return new Response(ResponseStatus.ERROR, message, null);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
