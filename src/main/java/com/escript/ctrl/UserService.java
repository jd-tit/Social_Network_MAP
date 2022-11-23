package com.escript.ctrl;

import com.escript.data.*;
import com.escript.domain.Friendship;
import com.escript.domain.Storable;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.EdgeNotFoundException;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipAlreadyRegisteredException;
import com.escript.exceptions.contextful.FriendshipDoesNotExistException;
import com.escript.exceptions.contextful.UserDoesNotExistException;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class UserService {
    private final FileRepository<Long, User> userRepo;
    private final Friendships friendshipRepo;
    private final Graph<Long> friendshipGraph;

    public UserService(){
        Path userDataPath = Path.of("users.csv");
        userRepo = new  FileRepository<>(userDataPath, userDataPath, User::fromCSV);
        friendshipRepo = new Friendships(new LongID_Generator(0), Path.of("friends.csv"));
        friendshipGraph = new Graph<>();

        userRepo.loadFromFile();
        friendshipRepo.loadFromFile();

        var maxUserID = userRepo.stream().map(Storable::getIdentifier).max(Long::compareTo);
        var maxFriendshipID = friendshipRepo.stream().map(Storable::getIdentifier).max(Long::compareTo);

        if(maxUserID.isEmpty())
            userRepo.setID_Generator(new LongID_Generator(0));
        else
            userRepo.setID_Generator(new LongID_Generator(maxUserID.get() + 1));


        if(maxFriendshipID.isEmpty())
            friendshipRepo.setID_Generator(new LongID_Generator(0));
        else
            friendshipRepo.setID_Generator(new LongID_Generator(maxFriendshipID.get() + 1));

        //build friendshipGraph
        userRepo.getAll().forEach(x -> {
            try {
                friendshipGraph.addNode(x.getIdentifier());
            } catch (DuplicateElementException e) {
                throw new RuntimeException(e);
            }
        });

        friendshipRepo.getAll().forEach(x -> {
            try {
                friendshipGraph.addEdge(x.getUserID1(), x.getUserID2());
            } catch (DuplicateElementException | ID_NotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public User getUserByID(Long id) {
        var user = userRepo.get(id);
        if (user == null) {
            return null;
        }
        return user.clone();
    }

    public Friendship getFriendshipByUserIDs(long id1, Long id2) {
        return friendshipRepo.getByMembers(id1, id2);
    }

    public void addUser(User user) throws DuplicateElementException {
        userRepo.add(user);
        friendshipGraph.addNode(user.getIdentifier());
    }

    public void removeUser(Long id) throws ID_NotFoundException {
        userRepo.remove(id);
        friendshipGraph.removeNode(id);
        for(Friendship f : friendshipRepo.friendshipsOf(id)) {
            friendshipRepo.remove(f.getIdentifier());
        }
    }

    public void markFriends(Long id1, Long id2)
            throws UserDoesNotExistException, FriendshipAlreadyRegisteredException, DuplicateElementException {
        if(id1.equals(id2))
            throw new DuplicateElementException(new String());

        try {
            friendshipGraph.addEdge(id1, id2);
        } catch (ID_NotFoundException e) {
            throw new UserDoesNotExistException();
        } catch (DuplicateElementException e) {
            throw new FriendshipAlreadyRegisteredException();
        }

        try {
            friendshipRepo.add(new Friendship(id1, id2, LocalDateTime.now()));
        } catch (DuplicateElementException e) {
            throw new FriendshipAlreadyRegisteredException();
        }
    }

    public void unmarkFriends(Long id1, Long id2) throws UserDoesNotExistException, FriendshipDoesNotExistException, DuplicateElementException {
        if(id1.equals(id2))
            throw new DuplicateElementException(new String());

        try {
            friendshipGraph.removeEdge(id1, id2);
        } catch (ID_NotFoundException e) {
            throw new UserDoesNotExistException();
        } catch (EdgeNotFoundException e) {
            throw new FriendshipDoesNotExistException();
        }

        friendshipRepo.removeByMembers(id1, id2);
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

        return longestChain.stream().map(userRepo::get).toList();
    }

    public void resetCurrentStreaks(){
        this.friendshipRepo.getAll().forEach(x -> x.setMessagingStreak(0));
        friendshipRepo.writeToFile();
    }

    public void incrementStreak(Long id1, Long id2) throws UserDoesNotExistException {
        if(userRepo.get(id1) == null || userRepo.get(id2) == null)
            throw new UserDoesNotExistException();

        var target = friendshipRepo.getByMembers(id1, id2);
        int newStreak = target.getMessagingStreak() + 1;
        target.setMessagingStreak(newStreak);
        friendshipRepo.writeToFile();
    }
}
