package com.escript.ctrl;

import com.escript.data.*;
import com.escript.domain.Friendship;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.EdgeNotFoundException;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipAlreadyRegisteredException;
import com.escript.exceptions.contextful.FriendshipDoesNotExistException;
import com.escript.exceptions.contextful.UserDoesNotExistException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class UserService {
    private final UserDBRepo userRepo;
    private final FriendshipDBRepo friendshipRepo;
    private final Graph<Long> friendshipGraph;

    public UserService(){
        userRepo = new UserDBRepo("Users");
        friendshipRepo = new FriendshipDBRepo("Friendships");
        friendshipGraph = new Graph<>();

        //build friendshipGraph
        userRepo.getAll().forEach(x ->
            friendshipGraph.addNode(x.getIdentifier())
        );

        friendshipRepo.getAll().forEach(x ->
            friendshipGraph.addEdge(x.getUserID1(), x.getUserID2())
        );
    }

    public User getUserByID(Long id) {
        try {
            var user = userRepo.get(id);
            return user.clone();
        } catch (ID_NotFoundException e) {
            return null;
        }
    }

    public Friendship getFriendshipByUserIDs(IdPair idPair) {
        try{
            return friendshipRepo.getByMembers(idPair.getFirst(), idPair.getSecond());
        } catch (ID_NotFoundException e) {
            return null;
        }
    }

    public void addUser(User user) throws DuplicateElementException {
        userRepo.add(user);
        User justAdded;
        justAdded = userRepo.getByName(user.getUsername());

        friendshipGraph.addNode(justAdded.getIdentifier());
    }

    public void removeUser(Long id) throws ID_NotFoundException {
        userRepo.remove(id);
        friendshipGraph.removeNode(id);
        for(Friendship f : friendshipRepo.friendshipsOf(id)) {
            friendshipRepo.remove(f.getIdentifier());
        }
    }

    public void markFriends(IdPair idPair)
            throws UserDoesNotExistException, FriendshipAlreadyRegisteredException, DuplicateElementException {
        try {
            friendshipGraph.addEdge(idPair.getFirst(), idPair.getSecond());
        } catch (ID_NotFoundException e) {
            throw new UserDoesNotExistException();
        } catch (DuplicateElementException e) {
            throw new FriendshipAlreadyRegisteredException();
        }

        try {
            friendshipRepo.add(
                    new Friendship(
                            idPair.getFirst(),
                            idPair.getSecond(),
                            LocalDateTime.now())
            );
        } catch (DuplicateElementException e) {
            throw new FriendshipAlreadyRegisteredException();
        }
    }

    public void unmarkFriends(IdPair idPair) throws UserDoesNotExistException, FriendshipDoesNotExistException, DuplicateElementException {
        try {
            friendshipGraph.removeEdge(idPair.getFirst(), idPair.getSecond());
        } catch (ID_NotFoundException e) {
            throw new UserDoesNotExistException();
        } catch (EdgeNotFoundException e) {
            throw new FriendshipDoesNotExistException();
        }

        friendshipRepo.removeByMembers(idPair.getFirst(), idPair.getSecond());
    }

    public Collection<User> getAllUsers() {
        return this.userRepo.getAll();
    }

    public Collection<Friendship> getAllFriendships() {
        return this.friendshipRepo.getAll();
    }

    public int countCommunities() {
        return this.friendshipGraph.countConnectedComponents();
    }

    public Iterable<User> getMostConnectedCommunity() {
        var communities = friendshipGraph.getConnectedComponents();
        ArrayList<Long> longestChain = new ArrayList<>();
        for(var community : communities){
            var friendChain = friendshipGraph.longestSimplePath(community);
            if(longestChain.size() < friendChain.size())
                longestChain = new ArrayList<>(friendChain);
        }

        return longestChain.stream().map(this::getUserByID).toList();
    }

    public void resetCurrentStreaks(){
        for(Friendship fr : friendshipRepo.getAll()) {
            fr.setMessagingStreak(0);
            friendshipRepo.update(fr);
        }
    }

    public void incrementStreak(IdPair idPair) throws UserDoesNotExistException {
        if(getUserByID(idPair.getFirst()) == null || getUserByID(idPair.getSecond()) == null)
            throw new UserDoesNotExistException();

        var target = this.getFriendshipByUserIDs(idPair);
        int newStreak = target.getMessagingStreak() + 1;
        target.setMessagingStreak(newStreak);
        friendshipRepo.update(target);
    }
}
