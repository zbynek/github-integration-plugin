package org.jenkinsci.plugins.github.pullrequest.publishers.impl;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Api;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import org.jenkinsci.plugins.github.pullrequest.publishers.GitHubPRAbstractPublisher;
import org.jenkinsci.plugins.github.pullrequest.utils.PublisherErrorHandler;
import org.jenkinsci.plugins.github.pullrequest.utils.StatusVerifier;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Closes pull request after build.
 *
 * @author Alina Karpovich
 */
public class GitHubPRClosePublisher extends GitHubPRAbstractPublisher {
    private static final Logger LOGGER = Logger.getLogger(GitHubPRClosePublisher.class.getName());

    @DataBoundConstructor
    public GitHubPRClosePublisher(StatusVerifier statusVerifier, PublisherErrorHandler errorHandler) {
        super(statusVerifier, errorHandler);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (getStatusVerifier() != null && !getStatusVerifier().isRunAllowed(build)) {
            return true;
        }

        String publishedURL = getTriggerDescriptor().getPublishedURL();
        if (publishedURL != null && !publishedURL.isEmpty()) {
            try {
                populate(build, launcher, listener);
                if (getGhIssue().getState().equals(GHIssueState.OPEN)) {
                    try {
                        getGhPullRequest().close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Couldn't close the pull request #" + getNumber() + ": '", ex);
                    }
                }
            } catch (IOException ex) {
                listener.getLogger().println("Can't close pull request \n" + ex.getMessage());
                handlePublisherError(build);
            }
        }
        return true;
    }

    public final Api getApi() {
        return new Api(this);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "GitHub PR: close PR";
        }
    }
}
