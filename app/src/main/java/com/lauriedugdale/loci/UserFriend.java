package com.lauriedugdale.loci;

/**
 * Created by mnt_x on 22/06/2017.
 */

public class UserFriend extends User {
    private boolean isAccepted;

    public UserFriend() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserFriend(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public UserFriend(User user, boolean isAccepted) {
        super(user.getUsername(), user.getEmail(), user.getDateJoined());
        this.isAccepted = isAccepted;
    }

    public UserFriend(String username, String email, Long dateJoined, boolean isAccepted) {
        super(username, email, dateJoined);
        this.isAccepted = isAccepted;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}
