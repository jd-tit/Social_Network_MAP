package com.escript.domain;

public class FriendshipDTO {
    private final Friendship friendship;
    private final String friendName;

    public FriendshipDTO(Friendship friendship, String friendName) {
        this.friendship = friendship;
        this.friendName = friendName;
    }

    public Friendship getFriendship() {
        return friendship;
    }

    public String getFriendName() {
        return friendName;
    }

    public Long getFriendId(Long userId) {
        if(friendship.getUserID1() != userId)
            return friendship.getUserID1();
        return friendship.getUserID2();
    }
}
