package com.xatkit.plugins.slack.platform.action;

import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.users.UsersGetPresenceRequest;
import com.github.seratch.jslack.api.methods.response.users.UsersGetPresenceResponse;
import com.xatkit.core.XatkitException;
import com.xatkit.core.platform.action.RuntimeAction;
import com.xatkit.core.session.XatkitSession;
import com.xatkit.execution.StateContext;
import com.xatkit.plugins.slack.platform.SlackPlatform;
import fr.inria.atlanmod.commons.log.Log;
import lombok.NonNull;

import java.io.IOException;

import static com.xatkit.plugins.slack.util.SlackUtils.logSlackApiResponse;
import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.isNull;

/**
 * Returns whether a given user in a given team is online.
 * <p>
 * This action accepts user ID, name, and real name.
 *
 * @see SlackPlatform#getUserId(String, String)
 */
public class IsOnline extends RuntimeAction<SlackPlatform> {

    /**
     * The name of the user to check.
     * <p>
     * This name can be either the user's ID, name, or real name.
     *
     * @see SlackPlatform#getUserId(String, String)
     */
    private String username;

    /**
     * The unique identifier of the Slack workspace containing the user to check.
     */
    private String teamId;

    /**
     * Constructs an {@link IsOnline} with the provided {@code runtimePlatform}, {@code session}, {@code username},
     * and {@code teamId}.
     *
     * @param platform the {@link SlackPlatform} containing this action
     * @param context  the {@link XatkitSession} associated to this action
     * @param username the name of the user to check
     * @param teamId   the unique identifier of the Slack workspace containing the user to check.
     */
    public IsOnline(@NonNull SlackPlatform platform, @NonNull StateContext context, @NonNull String username,
                    @NonNull String teamId) {
        super(platform, context);
        this.username = username;
        this.teamId = teamId;
    }

    /**
     * Returns whether the given user is online.
     *
     * @return {@code true} if the user is online, {@code false} otherwise
     * @throws XatkitException if an error occurred when accessing the Slack API
     */
    @Override
    protected Object compute() {

        String userId = this.runtimePlatform.getUserId(teamId, username);
        if (isNull(userId)) {
            Log.warn("Cannot find the user {0} in the team {1}, returning isOnline=false", username, teamId);
            return false;
        }
        UsersGetPresenceRequest request = UsersGetPresenceRequest.builder()
                .token(this.runtimePlatform.getSlackToken(teamId))
                .user(userId)
                .build();
        UsersGetPresenceResponse usersGetPresenceResponse;
        try {
            usersGetPresenceResponse = this.runtimePlatform.getSlack().methods().usersGetPresence(request);
        } catch (IOException | SlackApiException e) {
            throw new XatkitException("An error occurred when accessing the Slack API, see attached exception", e);
        }
        logSlackApiResponse(usersGetPresenceResponse);
        return usersGetPresenceResponse.getPresence().equals("active");
    }
}
