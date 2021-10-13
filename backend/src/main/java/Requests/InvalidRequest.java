package Requests;

import Responses.Response;

public class InvalidRequest implements IRequest{

    private int statusCode;

    InvalidRequest(){};

    // If we want to pass a status code when creating an invalid request
    InvalidRequest(int statusCode){
        this.statusCode = statusCode;
    }

    @Override
    public Response call() {
        return null;
    }
}