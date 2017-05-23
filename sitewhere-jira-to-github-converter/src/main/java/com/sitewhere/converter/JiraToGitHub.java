package com.sitewhere.converter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

/**
 * Main class for converting Jira tickets to GitHub issues.
 * 
 * @author Derek
 */
public class JiraToGitHub {

    private static final String DEFAULT_JIRA_URL = "https://sitewhere.atlassian.net/";
    private static final String DEFAULT_GITHUB_REPOSITORY = "sitewhere/sitewhere";

    /** Jira URL */
    private String jiraUrl;

    /** Jira Username */
    private String jiraUsername;

    /** Jira Password */
    private String jiraPassword;

    /** GitHub Repository */
    private String gitHubRepository;

    /** GitHub Username */
    private String gitHubUsername;

    /** GitHub Password */
    private String gitHubPassword;

    public static void main(String[] args) {
	String jiraUrl = (args.length > 0) ? args[0] : DEFAULT_JIRA_URL;
	String jiraUsername = (args.length > 1) ? args[1] : null;
	String jiraPassword = (args.length > 2) ? args[2] : null;
	String githubRepository = (args.length > 3) ? args[3] : DEFAULT_GITHUB_REPOSITORY;
	String githubUsername = (args.length > 4) ? args[4] : null;
	String githubPassword = (args.length > 5) ? args[5] : null;

	JiraToGitHub jtog = new JiraToGitHub();
	jtog.setJiraUrl(jiraUrl);
	jtog.setJiraUsername(jiraUsername);
	jtog.setJiraPassword(jiraPassword);
	jtog.setGitHubRepository(githubRepository);
	jtog.setGitHubUsername(githubUsername);
	jtog.setGitHubPassword(githubPassword);
	jtog.copyIssues();
    }

    /**
     * Copy all issues from Jira into GitHub.
     */
    public void copyIssues() {
	try {
	    SearchResult jiraResults = getJiraIssues();
	    Iterable<Issue> issues = jiraResults.getIssues();

	    GitHub github = GitHub.connectUsingPassword(getGitHubUsername(), getGitHubPassword());
	    GHMyself me = github.getMyself();
	    GHRepository sitewhere = github.getRepository(getGitHubRepository());

	    for (Issue issue : issues) {
		int number = Integer.parseInt(issue.getKey().substring(10));
		if (number != 12) {
		    System.out.println("Skipping issue " + issue.getKey());
		} else {
		    System.out.println("Converting issue " + issue.getKey());
		    GHIssue created = sitewhere.createIssue(issue.getSummary()).assignee(me)
			    .body(issue.getDescription()).create();
		    created.comment("From https://sitewhere.atlassian.net/browse/" + issue.getKey());
		    for (Comment comment : issue.getComments()) {
			created.comment(comment.getBody());
		    }
		    String typeLabel = issue.getIssueType().getName().toLowerCase();
		    created.setLabels(typeLabel);
		    created.close();

		    try {
			Thread.sleep(5 * 1000);
		    } catch (InterruptedException e) {
		    }
		}
	    }
	} catch (JiraAccessException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Get a list of all Jira issues.
     * 
     * @return
     * @throws JiraAccessException
     */
    protected SearchResult getJiraIssues() throws JiraAccessException {
	AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
	try {
	    URI jiraServerUri = new URI(getJiraUrl());
	    JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, getJiraUsername(),
		    getJiraPassword());
	    return restClient.getSearchClient().searchJql("project = SITEWHERE", 1000, 0, null).get();
	} catch (URISyntaxException e) {
	    throw new JiraAccessException(e);
	} catch (InterruptedException e) {
	    throw new JiraAccessException(e);
	} catch (ExecutionException e) {
	    throw new JiraAccessException(e);
	}
    }

    public String getJiraUrl() {
	return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
	this.jiraUrl = jiraUrl;
    }

    public String getJiraUsername() {
	return jiraUsername;
    }

    public void setJiraUsername(String jiraUsername) {
	this.jiraUsername = jiraUsername;
    }

    public String getJiraPassword() {
	return jiraPassword;
    }

    public void setJiraPassword(String jiraPassword) {
	this.jiraPassword = jiraPassword;
    }

    public String getGitHubRepository() {
	return gitHubRepository;
    }

    public void setGitHubRepository(String gitHubRepository) {
	this.gitHubRepository = gitHubRepository;
    }

    public String getGitHubUsername() {
	return gitHubUsername;
    }

    public void setGitHubUsername(String gitHubUsername) {
	this.gitHubUsername = gitHubUsername;
    }

    public String getGitHubPassword() {
	return gitHubPassword;
    }

    public void setGitHubPassword(String gitHubPassword) {
	this.gitHubPassword = gitHubPassword;
    }
}