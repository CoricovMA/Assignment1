package Requests.objects;

import Exceptions.AssignmentException;
import Responses.Response;
import Users.PollManager;

import javax.servlet.http.HttpServletRequest;

public class CloseRequest extends AbstractRequest implements Request {

    public CloseRequest(HttpServletRequest request){
        super(request);
    };


    /**
     * Implementation of the Close request. Closes the poll
     * @return Response object containing body and status request.
     */
    @Override
    public Response call() {

        try {

            PollManager.closePoll();

            return new Response().ok();

        } catch (AssignmentException e) {

            return new Response().serverError().exceptionBody(e);
        }

    }
}
