package com.example.mindbloomandroid.examples;

// EXAMPLE: How to display user info in activities

import com.example.mindbloomandroid.model.User;
import android.widget.TextView;

/**
 * Examples of using the updated User model
 * with displayId and formatted dates
 */
public class UserDisplayExamples {

    /**
     * Example 1: Display user info in profile screen
     */
    public void displayUserProfile(User user, TextView userIdText,
                                   TextView usernameText, TextView joinDateText) {

        // BEFORE (showed ugly long string and timestamp):
        // userIdText.setText(user.getUserId());  // "xK2j8mN9pQaRbS4tUv1wXyZ2"
        // joinDateText.setText(String.valueOf(user.getCreatedAt()));  // "1736459234567"

        // AFTER (shows clean display ID and readable date):
        userIdText.setText(user.getDisplayIdText());        // "User #1"
        usernameText.setText(user.getUsername());            // "testuser"
        joinDateText.setText("Member since: " +
                            user.getCreatedAtShort());      // "Member since: Jan 10, 2026"
    }

    /**
     * Example 2: Display in user dashboard welcome message
     */
    public void displayWelcomeMessage(User user, TextView welcomeText, TextView infoText) {
        // Show friendly welcome with display ID
        welcomeText.setText("Welcome back, " + user.getUsername() + "!");

        // Show account info
        infoText.setText(user.getDisplayIdText() + " • " +  // "User #5"
                        user.getRole() + " • " +              // "USER"
                        user.getCreatedAtShort());            // "Jan 10, 2026"
    }

    /**
     * Example 3: Display in RecyclerView adapter (user list)
     */
    public void bindUserInList(User user, TextView userNumberText,
                               TextView usernameText, TextView joinedText) {
        // For lists, use short formats
        userNumberText.setText(user.getDisplayIdText());    // "User #1"
        usernameText.setText(user.getUsername());            // "john_doe"
        joinedText.setText("Joined " +
                          user.getCreatedAtShort());        // "Joined Dec 15, 2025"
    }

    /**
     * Example 4: Display in instructor view (client list)
     */
    public void displayClientInfo(User client, TextView clientIdText,
                                  TextView clientNameText, TextView sinceDateText) {
        // For instructor viewing clients
        clientIdText.setText("Client " + client.getDisplayIdText());  // "Client User #3"
        clientNameText.setText(client.getUsername());
        sinceDateText.setText("Client since: " +
                             client.getFormattedCreatedAt());  // "Client since: Jan 10, 2026 2:30 PM"
    }

    /**
     * Example 5: Display in settings/account details
     */
    public void displayAccountDetails(User user, TextView displayIdText,
                                      TextView internalIdText,
                                      TextView createdFullText, TextView createdShortText) {
        // Show both IDs if needed for debugging/support
        displayIdText.setText("Display ID: " + user.getDisplayIdText());  // "Display ID: User #1"
        internalIdText.setText("Internal ID: " + user.getUserId());        // "Internal ID: xK2j8..."

        // Show both date formats
        createdFullText.setText("Full: " + user.getFormattedCreatedAt()); // "Full: Jan 10, 2026 2:30 PM"
        createdShortText.setText("Short: " + user.getCreatedAtShort());   // "Short: Jan 10, 2026"
    }

    /**
     * Example 6: Log user information for debugging
     */
    public void logUserInfo(User user) {
        android.util.Log.d("UserInfo", "=== USER INFO ===");
        android.util.Log.d("UserInfo", "Display ID: " + user.getDisplayIdText());
        android.util.Log.d("UserInfo", "Username: " + user.getUsername());
        android.util.Log.d("UserInfo", "Role: " + user.getRole());
        android.util.Log.d("UserInfo", "Created: " + user.getFormattedCreatedAt());
        android.util.Log.d("UserInfo", "Firebase UID: " + user.getUserId());
        android.util.Log.d("UserInfo", "================");
    }

    /**
     * Example 7: Format for notifications
     */
    public String formatNotificationText(User user, String action) {
        // Example: "User #5 (john_doe) liked your post"
        return user.getDisplayIdText() +
               " (" + user.getUsername() + ") " +
               action;
    }

    /**
     * Example 8: Format for chat messages
     */
    public String formatChatSender(User user) {
        // Example: "User #3 - john_doe"
        return user.getDisplayIdText() + " - " + user.getUsername();
    }

    /**
     * Example 9: Format for leaderboard/ranking
     */
    public void displayInLeaderboard(User user, int rank, TextView rankText,
                                     TextView userInfoText, TextView joinText) {
        rankText.setText("Rank #" + rank);
        userInfoText.setText(user.getDisplayIdText() + ": " + user.getUsername());
        joinText.setText("Joined: " + user.getCreatedAtShort());
    }

    /**
     * Example 10: Format for support/admin panel
     */
    public String formatForSupport(User user) {
        return "User Details:\n" +
               "Display ID: " + user.getDisplayIdText() + "\n" +
               "Username: " + user.getUsername() + "\n" +
               "Role: " + user.getRole() + "\n" +
               "Registered: " + user.getFormattedCreatedAt() + "\n" +
               "Firebase UID: " + user.getUserId();
    }
}

