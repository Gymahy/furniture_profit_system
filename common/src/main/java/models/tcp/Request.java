package models.tcp;

import enums.RequestType;
import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private RequestType requestType;
    private Object payload;

    public Request() {
    }

    public Request(RequestType requestType, Object payload) {
        this.requestType = requestType;
        this.payload = payload;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
