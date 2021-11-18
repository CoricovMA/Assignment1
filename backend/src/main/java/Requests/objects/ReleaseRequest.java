package Requests.objects;

import Exceptions.AssignmentException;
import Responses.Response;
import Users.PollManager;

public class ReleaseRequest implements Request {

    public ReleaseRequest(){};

    /**
     * Implementation of the Release request. Sets the poll status to released
     * @return Response object containing body and status request.
     */
    @Override
    public Response call() {
        try {
            PollManager.releasePoll();
            return new Response().ok();
        } catch (AssignmentException e) {
            return new Response().serverError().exceptionBody(e);
        }
    }
}
