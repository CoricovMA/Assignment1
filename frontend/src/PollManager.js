import React, {Component} from "react";
import './PollManager.css';
import axios from "axios";

/**
 * Class responsible for displaying and handling Poll Manager requests.
 */
class PollManager extends Component {
    /**
     * Constructor which allows functions to use the "this" keyword.     *
     * @param props
     */
    constructor(props) {
        super(props);
        this.handleCreate = this.handleCreate.bind(this);
        this.handleUpdate = this.handleUpdate.bind(this);
        this.handleClear = this.handleClear.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleRun = this.handleRun.bind(this);
        this.handleRelease = this.handleRelease.bind(this);
        this.handleUnrelease = this.handleUnrelease.bind(this);
    }

    /***
     * Function responsible for making a request to create a new poll.
     */
    handleCreate() {
        var getData = null; //Need to implement
        axios.post('http://localhost:8080/create', getData)
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    /**
     * Function responsible for sending a request to update a poll.
     */
    handleUpdate() {
        var getData = null; //Need to implement
        axios.post('http://localhost:8080/update', getData)
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    /**
     * Function responsible for sending a request to clear the poll.
     */
    handleClear() {
        axios.put('http://localhost:8080/clear')
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    /**
     * Function responsible for sending a request to close the poll.
     */
    handleClose() {
        axios.put('http://localhost:8080/close')
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    /**
     * Function responsible for sending a request to run the poll.
     */
    handleRun() {
        axios.put('http://localhost:8080/run')
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    /**
     * Function responsible for sending a request to release the poll.
     */
    handleRelease() {
        axios.put('http://localhost:8080/release')
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    /**
     * Function responsible for sending a request to unrelease the poll.
     */
    handleUnrelease() {
        axios.put('http://localhost:8080/unrelease')
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    /***
     * Function responsible for rendering tags for use in react methods.
     * @returns {JSX.Element}
     */
    render() {
        return (
            <div className="Manager">
                <header>
                    <button type="button" onClick={this.handleCreate}>Create Poll</button><br/>
                    <button type="button" onClick={this.handleUpdate}>Update Poll</button><br/>
                    <button type="button" onClick={this.handleClear}>Clear Poll</button><br/>
                    <button type="button" onClick={this.handleClose}>Close Poll</button><br/>
                    <button type="button" onClick={this.handleRun}>Run Poll</button><br/>
                    <button type="button" onClick={this.handleRelease}>Release Poll</button><br/>
                    <button type="button" onClick={this.handleUnrelease}>Unrelease Poll</button>
                </header>
            </div>
        );
    }
}

export default PollManager;
