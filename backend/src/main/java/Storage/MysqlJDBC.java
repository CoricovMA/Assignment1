package Storage;

import Exceptions.PollIsNotReleasedException;
import Polls.Poll;
import Storage.Entities.Choice;
import Storage.Entities.Vote;
import Users.User;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MysqlJDBC {

    private static Connection connection;
    private static MysqlJDBC INSTANCE;

    private static final String INSERT_USER_QUERY = "INSERT INTO Users (name, email, password) values (?, ?, ?)";
    private static final String INSERT_POLL_QUERY = "INSERT INTO Polls (pollId, title, question) values (?, ?, ?)";
    private static final String INSERT_CHOICE_QUERY = "INSERT INTO Choices (pollId, choice) values (?, ?)";
    private static final String INSERT_VOTE_QUERY = "INSERT INTO Vote (PIN, choiceId) values (?, ?)";

    private static final String UPDATE_USER_QUERY = "UPDATE Users SET name = ?, email = ?, password = ? WHERE userId = ?";
    private static final String UPDATE_POLL_QUERY = "Update Polls SET title = ?, question = ? WHERE pollId = ?";
    private static final String UPDATE_CHOICE_QUERY = "UPDATE Choices SET pollId = ?, choice = ? WHERE choiceId = ?";
    private static final String UPDATE_VOTE_QUERY = "UPDATE Vote SET PIN = ?, choiceId = ? WHERE voteId = ?";

    private static final String DELETE_USER_QUERY = "DELETE FROM Users WHERE userId = ?";
    private static final String DELETE_POLL_QUERY = "DELETE FROM Polls WHERE pollId = ?";
    private static final String DELETE_CHOICE_QUERY = "DELETE FROM Choices WHERE choiceId = ?";
    private static final String DELETE_VOTE_QUERY = "DELETE FROM Vote WHERE voteId = ?";

    private static final String SELECT_ALLUSER_QUERY = "SELECT * FROM Users";
    private static final String SELECT_USER_QUERY = "SELECT * FROM Users WHERE userId = ?";
    private static final String SELECT_ALLPOLL_QUERY = "SELECT * FROM Polls";
    private static final String SELECT_POLL_QUERY = "SELECT * FROM Polls WHERE pollId = ?";
    private static final String SELECT_ALLCHOICE_QUERY = "SELECT * FROM Choices";
    private static final String SELECT_CHOICE_QUERY = "SELECT * FROM Choices WHERE choiceId = ?";
    private static final String SELECT_POLLCHOICES_QUERY = "SELECT * FROM Choices WHERE pollId = ?";
    private static final String SELECT_ALLVOTE_QUERY = "SELECT * FROM Vote";
    private static final String SELECT_VOTE_QUERY = "SELECT * FROM Vote WHERE voteId = ?";

    public static MysqlJDBC getInstance() throws ClassNotFoundException, SQLException {
        if(connection == null || INSTANCE == null) {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager
                    .getConnection(String.format(Config.DB_STRING.value.toString(), Config.DB_PORT.value.toString())
                            , Config.DB_USERNAME.value.toString(), Config.DB_PW.value.toString());
            INSTANCE = new MysqlJDBC();
        }

        return INSTANCE;
    }

    /**
     * Method responsible for inserting a user into the database.
     *
     * @param user
     * @throws SQLException
     */
    public synchronized void insertUser(User user) throws SQLException {
        //USE THIS IF YOU NEED TO ACCESS AN AUTO GENERATED ID
        PreparedStatement statement = connection.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, user.fullName);
        statement.setString(2, user.emailAddress);
        statement.setString(3, user.hashedPassword);
        statement.executeUpdate();

        // HOW TO GET ACCESS TO AUTO GENERATED ID
        try (ResultSet rs = statement.getGeneratedKeys();) {
            int recordNum = -1;
            if (rs.next()) {
                recordNum = rs.getInt(1);
            }
            user.setUserId(Integer.toString(recordNum));
            System.out.println("New record ID is " + recordNum);
        }
        statement.close();
    }

    /**
     * Method responsible for inserting a poll into the database.
     *
     * @param poll
     * @throws SQLException
     */
    public synchronized void insertPoll(Poll poll) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(INSERT_POLL_QUERY);
        statement.setString(1, poll.getPollId());
        statement.setString(2, poll.getPollTitle());
        statement.setString(3, poll.getQuestionText());
        statement.executeUpdate();
        statement.close();
    }

    public synchronized Map<String, Long> getPollResults(String pollId) throws SQLException {
       List<Choice> choicesForPoll = this.selectPollChoices(pollId);

       return choicesForPoll.stream().collect(Collectors.groupingBy(Choice::getChoice, Collectors.counting() ));
    }

    public synchronized JSONObject getPollDetailsAsJson(String pollId) throws SQLException, PollIsNotReleasedException {
        Poll pollToCheck = this.selectPoll(pollId);
        if(pollToCheck.getStatus() == Poll.POLL_STATUS.RELEASED){
            JSONObject detailsJson = new JSONObject();
            detailsJson.put("state", pollToCheck.getState());
            detailsJson.put("votes", this.getPollResults(pollId));
            return detailsJson;
        }
        throw new PollIsNotReleasedException();
    }

    public synchronized String getPollDetailsAsString(String pollId) throws SQLException, PollIsNotReleasedException {
        Poll pollToCheck = this.selectPoll(pollId);
        if(pollToCheck.getStatus() == Poll.POLL_STATUS.RELEASED){
            StringBuilder sb = new StringBuilder();

            Map<String, Object> pollState = pollToCheck.getState();
            Map<String, Long> results = this.getPollResults(pollId);

            sb.append("Title: ").append(pollState.get("title")).append("\n");
            sb.append("Question: ").append(pollState.get("question")).append("\n");
            sb.append("State: ").append(pollState.get("state")).append("\n");
            sb.append("Choices: ").append(pollState.get("choices")).append("\n");
            sb.append("Voted: ").append(results.toString());
            return sb.toString();

        }
        throw new PollIsNotReleasedException();
    }

    /**
     * Method responsible for inserting a choice into the database.
     *
     * @param choice
     * @throws SQLException
     */
    public synchronized void insertChoice(Choice choice) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(INSERT_CHOICE_QUERY, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, choice.getPollId());
        statement.setString(2, choice.getChoice());
        statement.executeUpdate();

        try (ResultSet rs = statement.getGeneratedKeys();) {
            int recordNum = -1;
            if (rs.next()) {
                recordNum = rs.getInt(1);
            }
            choice.setChoiceID(Integer.toString(recordNum));
            System.out.println("New record ID is " + recordNum);
        }
        statement.close();
    }

    /**
     * Method responsible for inserting a vote into the database.
     *
     * @param vote
     * @throws SQLException
     */
    public synchronized void insertVote(Vote vote) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(INSERT_VOTE_QUERY, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, vote.getPIN());
        statement.setString(2, vote.getChoiceId());
        statement.executeUpdate();

        try (ResultSet rs = statement.getGeneratedKeys();) {
            int recordNum = -1;
            if (rs.next()) {
                recordNum = rs.getInt(1);
            }
            vote.setVoteId(Integer.toString(recordNum));
            System.out.println("New record ID is " + recordNum);
        }
        statement.close();
    }

    /**
     * Method responsible for updating a user in the database.
     *
      * @param user
     * @throws SQLException
     */
    public synchronized void updateUser(User user) throws SQLException {
        //USE THIS IF YOU NEED TO ACCESS AN AUTO GENERATED ID
        PreparedStatement statement = connection.prepareStatement(UPDATE_USER_QUERY);
        statement.setString(1, user.fullName);
        statement.setString(2, user.emailAddress);
        statement.setString(3, user.hashedPassword);
        statement.setString(4, user.userId);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for updating a poll in the database.
     *
     * @param poll
     * @throws SQLException
     */
    public synchronized void updatePoll(Poll poll) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_POLL_QUERY);
        statement.setString(1, poll.getPollTitle());
        statement.setString(2, poll.getQuestionText());
        statement.setString(3, poll.getPollId());
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for updating a choice in the database.
     *
     * @param choice
     * @throws SQLException
     */
    public synchronized void updateChoice(Choice choice) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_CHOICE_QUERY);
        statement.setString(1, choice.getPollId());
        statement.setString(2, choice.getChoice());
        statement.setString(2, choice.getChoiceID());
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for updating a vote in the database.
     *
     * @param vote
     * @throws SQLException
     */
    public synchronized void updateVote(Vote vote) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_VOTE_QUERY);
        statement.setString(1, vote.getPIN());
        statement.setString(2, vote.getChoiceId());
        statement.setString(3, vote.getVoteId());
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for deleting a user in the database.
     *
     * @param userId
     * @throws SQLException
     */
    public synchronized void deleteUser(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(DELETE_USER_QUERY);
        statement.setString(1, userId);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for deleting a poll in the database.
     *
     * @param pollId
     * @throws SQLException
     */
    public synchronized void deletePoll(String pollId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(DELETE_POLL_QUERY);
        statement.setString(1, pollId);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for deleting a choice in the database.
     *
     * @param choiceId
     * @throws SQLException
     */
    public synchronized void deleteChoice(String choiceId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(DELETE_CHOICE_QUERY);
        statement.setString(1, choiceId);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for deleting a vote in the database.
     *
     * @param voteId
     * @throws SQLException
     */
    public synchronized void deleteVote(String voteId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(DELETE_VOTE_QUERY);
        statement.setString(1, voteId);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Method responsible for returning a list of all users in the database.
     *
     * @return
     * @throws SQLException
     */
    public synchronized List<User> selectAllUsers() throws SQLException {
        List<User> rows = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(SELECT_ALLUSER_QUERY);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = setupUser(resultSet);
                rows.add(user);
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return rows;
    }

    /**
     * Method responsible for returning a specific user in the database.
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public synchronized User selectUser(String userId) throws SQLException {
        User user = null;
        PreparedStatement statement = connection.prepareStatement(SELECT_USER_QUERY);
        statement.setString(1, userId);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                user = setupUser(resultSet);
            }
        }
        catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return user;
    }

    /**
     * Method responsible for returning a list of all polls in the database.
     *
     * @return
     * @throws SQLException
     */
    public synchronized List<Poll> selectAllPolls() throws SQLException {
        List<Poll> rows = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(SELECT_ALLPOLL_QUERY);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Poll poll = setupPoll(resultSet);
                rows.add(poll);
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return rows;
    }

    /**
     * Method responsible for returning a specific poll in the database.
     *
     * @param pollId
     * @return
     * @throws SQLException
     */
    public synchronized Poll selectPoll(String pollId) throws SQLException {
        Poll poll = null;
        PreparedStatement statement = connection.prepareStatement(SELECT_POLL_QUERY);
        statement.setString(1, pollId);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                poll = setupPoll(resultSet);
            }
        }
        catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return poll;
    }

    /**
     * Method responsible for returning a list of all choices in the database.
     *
     * @return
     * @throws SQLException
     */
    public synchronized List<Choice> selectAllChoices() throws SQLException {
        List<Choice> rows = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(SELECT_ALLCHOICE_QUERY);
        return getChoices(rows, statement);
    }

    private List<Choice> getChoices(List<Choice> rows, PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Choice choice = setupChoice(resultSet);
                rows.add(choice);
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return rows;
    }

    /**
     * Method responsible for returning a specific choice in the database.
     *
     * @param choiceId
     * @return
     * @throws SQLException
     */
    public synchronized Choice selectChoice(String choiceId) throws SQLException {
        Choice choice = null;
        PreparedStatement statement = connection.prepareStatement(SELECT_CHOICE_QUERY);
        statement.setString(1, choiceId);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                choice = setupChoice(resultSet);
            }
        }
        catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return choice;
    }

    /**
     * Method responsible for returning a list of all choices from a specific poll in the database.
     *
     * @param pollId
     * @return
     * @throws SQLException
     */
    public synchronized List<Choice> selectPollChoices(String pollId) throws SQLException {
        List<Choice> rows = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(SELECT_POLLCHOICES_QUERY);
        statement.setString(1, pollId);
        return getChoices(rows, statement);
    }

    /**
     * Method responsible for returning a list of all votes in the database.
     *
     * @return
     * @throws SQLException
     */
    public synchronized List<Vote> selectAllVotes() throws SQLException {
        List<Vote> rows = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(SELECT_ALLVOTE_QUERY);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Vote vote = setupVote(resultSet);
                rows.add(vote);
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return rows;
    }

    /**
     * Method responsible for returning a specific vote in the database.
     *
     * @param voteId
     * @return
     * @throws SQLException
     */
    public synchronized Vote selectVote(String voteId) throws SQLException {
        Vote vote = null;
        PreparedStatement statement = connection.prepareStatement(SELECT_VOTE_QUERY);
        statement.setString(1, voteId);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                vote = setupVote(resultSet);
            }
        }
        catch (SQLException ex) {
            System.out.println("Exception: " + ex);
            throw ex;
        }
        statement.close();
        return vote;
    }

    /**
     * Method responsible for setting up a user object using data from the database.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public synchronized User setupUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getString("userId"));
        user.setFullName(rs.getString("name"));
        user.setEmailAddress(rs.getString("email"));
        user.setHashedPassword(rs.getString("password"));
        return user;
    }

    /**
     * Method responsible for setting up a poll object using data from the database.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public synchronized Poll setupPoll(ResultSet rs) throws SQLException {
        Poll poll = new Poll();
        poll.setPollId(rs.getString("pollId"));
        poll.setPollTitle(rs.getString("title"));
        poll.setQuestionText(rs.getString("question"));
        poll.setPollStatus(rs.getString("pollStatus"));
        return poll;
    }

    /**
     * Method responsible for setting up a choice object using data from the database.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public synchronized Choice setupChoice(ResultSet rs) throws SQLException {
        Choice choice = new Choice();
        choice.setChoiceID(rs.getString("choiceId"));
        choice.setPollId(rs.getString("pollId"));
        choice.setChoice(rs.getString("choice"));
        return choice;
    }

    /**
     * Method responsible for setting up a vote object using data from the database.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public synchronized Vote setupVote(ResultSet rs) throws SQLException {
        Vote vote = new Vote();
        vote.setVoteId(rs.getString("voteId"));
        vote.setPIN(rs.getString("PIN"));
        vote.setChoiceId("choiceId");
        return vote;
    }
}
