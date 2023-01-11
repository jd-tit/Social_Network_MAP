package com.escript.domain;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

public class Friendship extends Storable<Long> {
    private long userID1;
    private long userID2;
    private final LocalDateTime friendsSince;

    private int messagingStreak;
    private int longestMessagingStreak;

    public void setUserID1(long userID1) {
        this.userID1 = userID1;
    }

    public void setUserID2(long userID2) {
        this.userID2 = userID2;
    }
    public int getMessagingStreak() {
        return messagingStreak;
    }

    public void setMessagingStreak(int messagingStreak) {
        if (messagingStreak > longestMessagingStreak)
            longestMessagingStreak = messagingStreak;
        this.messagingStreak = messagingStreak;
    }

    public int getLongestMessagingStreak() {
        return longestMessagingStreak;
    }

    public Friendship(long friendshipID, long userID1, long userID2, LocalDateTime friendsSince, int currentStreak, int bestStreak) {
        super.setIdentifier(friendshipID);
        this.userID1 = userID1;
        this.userID2 = userID2;
        this.friendsSince = friendsSince;
        this.messagingStreak = currentStreak;
        this.longestMessagingStreak = bestStreak;
    }

    public Friendship(long userID1, long userID2, LocalDateTime friendsSince) {
        this.userID1 = userID1;
        this.userID2 = userID2;
        this.friendsSince = friendsSince;
        this.messagingStreak = 0;
        this.longestMessagingStreak = 0;
    }

    public long getUserID1() {
        return userID1;
    }

    public long getUserID2() {
        return userID2;
    }

    public LocalDateTime getFriendsSince() {
        return friendsSince;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return  userID1 == that.userID1 && userID2 == that.userID2 ||
                userID1 == that.userID2 && userID2 == that.userID1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID1, userID2);
    }

//    @Override
//    public String toCSV() {
//        return String.join(",",
//                Long.toString(getIdentifier()),
//                Long.toString(userID1),
//                Long.toString(userID2),
//                friendsSince.toString(),
//                Integer.toString(messagingStreak),
//                Integer.toString(longestMessagingStreak)
//        );
//    }

    public static Friendship fromCSV(String string) {
        var parts = string.split(",");
        long identifier = Long.parseLong(parts[0]);
        long id1 = Long.parseLong(parts[1]);
        long id2 = Long.parseLong(parts[2]);
        LocalDateTime friendsSince = LocalDateTime.parse(parts[3]);
        int currentStreak = Integer.parseInt(parts[4]);
        int longestStreak = Integer.parseInt(parts[5]);

        var result = new Friendship(id1, id2, friendsSince);
        result.messagingStreak = currentStreak;
        result.longestMessagingStreak = longestStreak;
        result.setIdentifier(identifier);
        return result;
    }
}
