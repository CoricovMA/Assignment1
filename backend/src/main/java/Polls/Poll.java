package Polls;

import Exceptions.AssignmentException;
import Exceptions.InvalidPollStateException;
import Util.StringHelper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Poll implements Serializable {

    public enum POLL_STATUS {
        CREATED("created"),
        RUNNING("running"),
        RELEASED("released"),
        CLOSED("closed");

        private final String value;

        POLL_STATUS(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    @JsonProperty("name")
    private String pollTitle;

    @JsonProperty("question")
    private String questionText;

    @JsonProperty("choices")
    private List<String> choicesList;

    @JsonProperty("id")
    private String pollId;

    @JsonProperty("pollStatus")
    private POLL_STATUS pollStatus;

    public Poll(){
        this.pollStatus = POLL_STATUS.CREATED;
    }

    public Poll(String name, String question, List<String> choices) {
        this.choicesList = choices;
        this.pollTitle = name;
        this.questionText = question;
        this.pollId = StringHelper.randomID();
        this.pollStatus = POLL_STATUS.CREATED;
    }

    public String getPollTitle() {
        return pollTitle;
    }

    public void setPollTitle(String pollTitle) {
        this.pollTitle = pollTitle;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getChoicesList() {
        return choicesList;
    }

    public void setChoicesList(List<String> choicesList) {
        this.choicesList = choicesList;
    }

    public String getPollId(){
        return this.pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public POLL_STATUS getStatus() {
        return pollStatus;
    }

    public void setPollStatus(String status){
        this.pollStatus = POLL_STATUS.valueOf(status.toUpperCase());
    }

    public void setPollStatus(POLL_STATUS status){
        this.pollStatus = status;
    }

    // it is a good practice to implement equals and hash when implementing serializable
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poll poll = (Poll) o;
        return pollTitle.equals(poll.pollTitle) &&
                questionText.equals(poll.questionText) &&
                choicesList.equals(poll.choicesList) &&
                pollId.equals(poll.pollId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pollTitle, questionText, choicesList, pollId);
    }

    /**
     * Helper method which throws an exception if the current status is invalid.
     * @param wantedStatus given wanted status, throws an error if current status != wanted stats
     * @param triedAction action which was tried and invalid in current state
     * @throws AssignmentException
     */
    public void checkPollState(POLL_STATUS wantedStatus, String triedAction) throws AssignmentException{
        if(this.pollStatus != wantedStatus)
            throw new InvalidPollStateException(this.pollStatus.value, triedAction);
    }

    /**
     * Returns the current state of the poll, more of a helper method for populating the frontend
     * @return
     */
    public Map<String, Object> getState() {
        Map<String, Object> mapToReturn = new HashMap<>();

        if (this.pollStatus != null) {
            mapToReturn.put("choices", this.getChoicesList());
            mapToReturn.put("question", this.getQuestionText());
            mapToReturn.put("title", this.getPollTitle());
        }

        mapToReturn.put("state", Objects.requireNonNull(this.pollStatus).value);

        return mapToReturn;
    }


}
