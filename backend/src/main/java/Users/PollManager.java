package Users;

import Exceptions.*;
import Polls.Poll;
import Storage.Entities.Choice;
import Storage.Entities.Vote;
import Storage.MysqlJDBC;
import Util.SessionManager;
import Util.StringHelper;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;
import org.json.XML;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PollManager {

    public synchronized static void createPoll(Poll pollToInsert)
            throws SQLException, ClassNotFoundException {
        List<Choice> choiceList = pollToInsert.getChoicesList()
                .stream()
                .map(choice -> new Choice(StringHelper.randomPin() ,pollToInsert.getPollId(),choice))
                .collect(Collectors.toList());

        MysqlJDBC.getInstance().insertPoll(pollToInsert);

        for(Choice choice: choiceList){
            MysqlJDBC.getInstance().insertChoice(choice);
        }

    }

    public synchronized static void updatePoll(String name, String question, List<String> choices, String pollId) throws InvalidPollStateException, SQLException, ClassNotFoundException {
        // can only update a poll if it's already running
        Poll pollInstance = MysqlJDBC.getInstance().selectPoll(pollId);
        if (pollInstance == null || (pollInstance.getStatus() != Poll.POLL_STATUS.CREATED
                && pollInstance.getStatus() != Poll.POLL_STATUS.RUNNING))
            throw new InvalidPollStateException(Objects.requireNonNull(pollInstance).getStatus().getValue(), "update");

        pollInstance.setPollStatus(Poll.POLL_STATUS.CREATED);
        pollInstance.setPollTitle(name);
        pollInstance.setQuestionText(question);
        pollInstance.setChoicesList(choices);
        MysqlJDBC.getInstance().deleteAllVotesFromPoll(pollId);
    }

    /**
     * This method clears the poll if the poll is released or running.
     * Else, it throws invalid state exceptions
     * @throws InvalidPollStateException
     */
    public synchronized static void clearPoll(String pollId) throws InvalidPollStateException, SQLException, ClassNotFoundException {
        Poll pollToCheck = MysqlJDBC.getInstance().selectPoll(pollId);
        Poll.POLL_STATUS currentStatus = pollToCheck.getStatus();
        if (currentStatus !=  Poll.POLL_STATUS.RELEASED && currentStatus !=  Poll.POLL_STATUS.RUNNING)
            throw new InvalidPollStateException(currentStatus.getValue(), "clear");

        if (currentStatus == Poll.POLL_STATUS.RELEASED) {
            MysqlJDBC.getInstance().deleteAllVotesFromPoll(pollId);
            pollToCheck.setChoicesList(new ArrayList<>());
            pollToCheck.setPollStatus(Poll.POLL_STATUS.CREATED);
            MysqlJDBC.getInstance().updatePoll(pollToCheck);
        } else {
            MysqlJDBC.getInstance().deleteAllVotesFromPoll(pollId);
        }
    }

    public synchronized static void closePoll(String pollId) throws AssignmentException, SQLException, ClassNotFoundException {
        MysqlJDBC.getInstance().updatePollStatus(pollId,
                Poll.POLL_STATUS.CLOSED,
                Poll.POLL_STATUS.RELEASED,
                "close");
    }

    public synchronized static void runPoll(String pollId) throws AssignmentException, SQLException, ClassNotFoundException {
        MysqlJDBC.getInstance().updatePollStatus(pollId,
                Poll.POLL_STATUS.RUNNING,
                Poll.POLL_STATUS.CREATED,
                "run");
    }

    public synchronized static void releasePoll(String pollId) throws AssignmentException, SQLException, ClassNotFoundException {
        MysqlJDBC.getInstance().updatePollStatus(pollId,
                Poll.POLL_STATUS.RELEASED,
                Poll.POLL_STATUS.RUNNING,
                "release");
    }

    public synchronized static void unreleasePoll(String pollId) throws AssignmentException, SQLException, ClassNotFoundException {
        MysqlJDBC.getInstance().updatePollStatus(pollId,
                Poll.POLL_STATUS.RUNNING,
                Poll.POLL_STATUS.RELEASED,
                "unrelease");
    }

    /**
     * This method allows the participant to vote if the Poll state is running, else, it throws an exception.
     * @param httpSession given HttpSession from the Servlet
     * @param choice given choice fromm the participant
     * @throws AssignmentException
     */
    public synchronized static void vote(HttpSession httpSession, String choice, String pin, String pollId ) throws AssignmentException, SQLException, ClassNotFoundException {
        if (choice.isBlank() || choice.isEmpty() || !PollManager.validateChoice(choice, pollId))
            throw new InvalidChoiceException();

        Poll pollToVote = MysqlJDBC.getInstance().selectPoll(pollId);
        pollToVote.checkPollState(Poll.POLL_STATUS.RUNNING, "vote");

    }

    /**
     * Returns the results of the poll
     * @return map containing choice, vote-count value pairs
     */
    public static Map<String, Long> getPollResults(String pollId) throws SQLException, ClassNotFoundException {
        return MysqlJDBC.getInstance().getPollResults(pollId);
    }

    public synchronized static JSONObject downloadJSonPollDetails(String pollId) throws PollIsNotReleasedException, SQLException, ClassNotFoundException {
        return MysqlJDBC.getInstance().getPollDetailsAsJson(pollId);
    }

    public synchronized static String downloadStringPollDetails(String pollId) throws AssignmentException, SQLException, ClassNotFoundException {
        return MysqlJDBC.getInstance().getPollDetailsAsString(pollId);
    }

    public synchronized static String downloadXMLPollDetails(String pollId) throws PollIsNotReleasedException, SQLException, ClassNotFoundException {
        return XML.toString(MysqlJDBC.getInstance().getPollDetailsAsJson(pollId));
    }

    public static Optional<String> getPollTitle(String pollId) throws SQLException, ClassNotFoundException {
        return Optional.of(MysqlJDBC.getInstance().selectPoll(pollId).getPollTitle());
    }

    /**
     * Validates a give choice. If invalid, throws an error, else returns a boolean
     * @param choice
     * @return
     * @throws InvalidPollStateException
     */
    public synchronized static boolean validateChoice(String choice, String pollId) throws InvalidPollStateException, SQLException, ClassNotFoundException {
        Poll pollInstance = MysqlJDBC.getInstance().selectPoll(pollId);
        if (pollInstance == null)
            throw new InvalidPollStateException("none", "vote");
        return pollInstance.getChoicesList().contains(choice);
    }

//    /**
//     * Changes the vote of a participant who already voted.
//     * @param httpSession given HttpSession from the servlet
//     * @param choice choice
//     */
//    private static void changeVote(HttpSession httpSession, Vote vote) {
//        if(httpSession.getAttribute("userId") != null){
//
//        }
//        JSONObject oldChoice = httpSession.getAttribute("vote").toString();
//
//    }

    public static Map<String, Object> getState(String pollId) throws SQLException, ClassNotFoundException {
        return MysqlJDBC.getInstance().selectPoll(pollId).getState();
    }
}
