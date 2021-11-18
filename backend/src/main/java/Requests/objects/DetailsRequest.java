package Requests.objects;

import Exceptions.AssignmentException;
import Responses.Response;
import Users.PollManager;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;


/**
 * Like the Results request, the Details request requires no permissions
 */
public class DetailsRequest extends AbstractRequest implements Request {

    public DetailsRequest(HttpServletRequest request){
        super(request);
    };


    /**
     * Implementation of the Details request. Returns the details as file
     * @return Response object containing body and status request.
     */
    @Override
    public Response call() {
        Response toReturn = new Response();
        String pollTitle = PollManager.getPollTitle().orElseGet(() ->"empty");
        String extension = (getRequest().getAttribute("extension") == null)
                ? ".txt"
                : getRequest().getAttribute("extension").toString();
        toReturn.addHeader("Content-disposition", String.format("attachment; filename=%s-%d%s", pollTitle, PollManager.getPollReleasedTimestamp(), extension));
        String choice = this.getRequest().getParameter("choice");
        String pollId = this.getRequest().getParameter("pollId");
        try{
            if (choice.equals("JSON")) {
                JSONObject obj = PollManager.downloadJSonPollDetails(pollId);
                toReturn.setBody(obj.toString(2)); // For use with JSON
            }
            else if (choice.equals("TEXT")){
                String str = PollManager.downloadStringPollDetails(pollId);
                toReturn.setBody(str);
            }
            else{
                String xml = PollManager.downloadXMLPollDetails(pollId);
                toReturn.setBody(xml);
            }
            return toReturn;
        } catch (AssignmentException | SQLException | ClassNotFoundException  e) {

            return new Response().serverError().exceptionBody(e);
        }
    }
}
