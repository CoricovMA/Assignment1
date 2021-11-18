package Requests.objects;

import Exceptions.AssignmentException;
import Responses.Response;
import Users.PollManager;

public class UnreleaseRequest implements Request {

    public UnreleaseRequest(){};

    /**
     * Implementation of the Unrelease request. Unreleases the poll
     * @return Response object containing body and status request.
     */
    @Override
    public Response call() {
        try {
            PollManager.unreleasePoll();
            return new Response().ok();
        } catch (AssignmentException e) {
            return new Response().badRequest().exceptionBody(e);
        }
    }
}
