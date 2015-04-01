package org.jenkinsci.plugins.github.pullrequest.events.impl;

import hudson.Extension;
import org.jenkinsci.plugins.github.pullrequest.GitHubPRLabel;
import org.jenkinsci.plugins.github.pullrequest.GitHubPRPullRequest;
import org.jenkinsci.plugins.github.pullrequest.GitHubPRTrigger;
import org.jenkinsci.plugins.github.pullrequest.events.GitHubPREvent;
import org.jenkinsci.plugins.github.pullrequest.events.GitHubPREventDescriptor;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Used to skip PR builder. Use case is skipping if PR is marked with some label or labels.
 */
public class GitHubPRSkipLabelEvent extends GitHubPREvent {
    private final static Logger LOGGER = Logger.getLogger(GitHubPRSkipLabelEvent.class.getName());

    private final GitHubPRLabel skipLabel;

    @DataBoundConstructor
    public GitHubPRSkipLabelEvent(GitHubPRLabel skipLabel) {
        this.skipLabel = skipLabel;
    }

    /**
     * Checks for skip buildAndComment phrase in pull request comment. If present it updates shouldRun as false.
     *
     * @param remotePR {@link org.kohsuke.github.GHIssue} that contains comments to check
     */
    @Override
    public boolean isSkip(GitHubPRTrigger gitHubPRTrigger, GHPullRequest remotePR, GitHubPRPullRequest localPR) throws IOException {
        for (GHLabel label : remotePR.getRepository().getIssue(remotePR.getNumber()).getLabels()) {
            for (String skipBuildLabel : skipLabel.getLabelsSet()) {
                skipBuildLabel = skipBuildLabel.trim();
                Pattern skipBuildLabelPattern = Pattern.compile(skipBuildLabel);
                if (skipBuildLabelPattern.matcher(label.getName()).matches()) {
                    LOGGER.log(Level.INFO, "Pull request is marked with {0} skipBuildLabel. Hence skipping the buildAndComment.",
                            skipBuildLabel);
                    return true;
                }
            }
        }

        return false;
    }

    public GitHubPRLabel getSkipLabel() {
        return skipLabel;
    }

    @Extension
    public static class DescriptorImpl extends GitHubPREventDescriptor {
        @Override
        public String getDisplayName() {
            return "Skip label";
        }
    }
}
