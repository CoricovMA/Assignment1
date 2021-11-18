package Users;

import Exceptions.*;
import Polls.Poll;
import Storage.Entities.Choice;
import Storage.MysqlJDBC;
import Util.SessionManager;
import Util.StringHelper;
import org.json.JSONObject;
import org.json.XML;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PollManager {



    // key would be unique identifier (sessionId, wtv) and value would be the vote choice
    private static final Map<String, String> submittedVotes = new HashMap<>();  //submitted votes, session id as key
    private static final Map<String, Integer> voteCount = new HashMap<>();      // count of different votes
    private static Poll pollInstance;
    private static long pollReleasedTimestamp;

    public synchronized static void createPoll(String name, String question, List<String> choices)
            throws SQLException, ClassNotFoundException {
        Poll pollToInsert = new Poll(name, question, choices);

        List<Choice> choiceList = choices
                .stream()
                .map(choice -> new Choice(StringHelper.randomPin() ,pollToInsert.getPollId(),choice))
                .collect(Collectors.toList());

        MysqlJDBC.getInstance().insertPoll(new Poll(name, question, choices));

        for(Choice choice: choiceList){
            MysqlJDBC.getInstance().insertChoice(choice);
        }

    }

    public synchronized static void updatePoll(String name, String question, List<String> choices, String pollId) throws InvalidPollStateException {
        // can only update a poll if it's already running
        if (pollInstance == null || (currentStatus != POLL_STATUS.CREATED && currentStatus != POLL_STATUS.RUNNING))
            throw new InvalidPollStateException(currentStatus.value, "update");

        pollInstance = new Poll(name, question, choices);
        currentStatus = POLL_STATUS.CREATED;
        clearChoices();
        addChoices(choices);
    }

    /**
     * This method clears the poll if the poll is released or running.
     * Else, it throws invalid state exceptions
     * @throws InvalidPollStateException
     */
    public synchronized static void clearPoll() throws InvalidPollStateException {
        if (pollInstance == null)
            throw new InvalidPollStateException("null", "close");

        if (currentStatus != POLL_STATUS.RELEASED && currentStatus != POLL_STATUS.RUNNING)
            throw new InvalidPollStateException(currentStatus.value, "clear");

        if (currentStatus == Poll.POLL_STATUS.RELEASED) {
            clearChoices();
            currentStatus = Poll.POLL_STATUS.CREATED;
        } else {
            clearChoices();
        }
    }

    public synchronized static void closePoll(String pollId) throws AssignmentException, SQLException, ClassNotFoundException {
        Poll pollToCheck = MysqlJDBC.getInstance().selectPoll(pollId);
        pollToCheck.checkPollState(Poll.POLL_STATUS.RELEASED, "close");
        pollToCheck.setPollStatus(Poll.POLL_STATUS.CLOSED);
        MysqlJDBC.getInstance().updatePoll(pollToCheck);
    }

    public synchronized static void runPoll(String pollId) throws AssignmentException, SQLException, ClassNotFoundException {
        Poll pollToCheck = MysqlJDBC.getInstance().selectPoll(pollId);
        pollToCheck.checkPollState(Poll.POLL_STATUS.CREATED, "run");
        pollToCheck.setPollStatus(Poll.POLL_STATUS.RUNNING);
        MysqlJDBC.getInstance().updatePoll(pollToCheck);
    }

    public synchronized static void releasePoll(String pollId) throws AssignmentException {
        pollReleasedTimestamp = System.currentTimeMillis();
        

        checkPollState(POLL_STATUS.RUNNING, "release");

        currentStatus = POLL_STATUS.RELEASED;
    }

    public synchronized static void unreleasePoll() throws AssignmentException {
        checkPollState(POLL_STATUS.RELEASED, "unrelease");

        currentStatus = POLL_STATUS.RUNNING;
    }



    public static String getPollId() {
        if (pollInstance != null) {
            return pollInstance.getPollId();
        }
        return "";
    }

    /**
     * This method allows the participant to vote if the Poll state is running, else, it throws an exception.
     * @param httpSession given HttpSession from the Servlet
     * @param choice given choice fromm the participant
     * @throws AssignmentException
     */
    public synchronized static void vote(HttpSession httpSession, String choice, String pollId, String votePin) throws AssignmentException {
        if (choice.isBlank() || choice.isEmpty() || !PollManager.validateChoice(choice))
            throw new InvalidChoiceException();

        checkPollState(POLL_STATUS.RUNNING, "vote");

        if (submittedVotes.containsKey(httpSession.getId()))
            changeVote(httpSession, choice);
        else
            voteCount.put(choice, voteCount.get(choice) + 1);

        submittedVotes.put(httpSession.getId(), choice);
        SessionManager.vote(httpSession, choice);
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

    public static Optional<String> getPollTitle() {
        return Optional.of(pollInstance.getPollTitle());
    }

    /**
     * Validates a give choice. If invalid, throws an error, else returns a boolean
     * @param choice
     * @return
     * @throws InvalidPollStateException
     */
    public synchronized static boolean validateChoice(String choice) throws InvalidPollStateException {
        if (pollInstance == null)
            throw new InvalidPollStateException("none", "vote");
        return pollInstance.getChoicesList().contains(choice);
    }

    public static long getPollReleasedTimestamp() {
        return pollReleasedTimestamp;
    }

    private synchronized static void clearChoices() {
        submittedVotes.clear();
        voteCount.clear();
    }

    /**
     * Changes the vote of a participant who already voted.
     * @param httpSession given HttpSession from the servlet
     * @param choice choice
     */
    private static void changeVote(HttpSession httpSession, String choice) {
        String oldChoice = httpSession.getAttribute("choice").toString();
        voteCount.put(oldChoice, voteCount.get(oldChoice) - 1);
        voteCount.put(choice, voteCount.get(choice) + 1);
    }

    private static void addChoices(List<String> choices) {
        synchronized (voteCount) {
            choices.forEach(item -> {
                voteCount.put(item, 0);
            });
        }

    }



}
