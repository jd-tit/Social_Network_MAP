package com.escript.service;

import com.escript.data.*;
import com.escript.domain.*;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.EdgeNotFoundException;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipAlreadyRegisteredException;
import com.escript.exceptions.contextful.FriendshipDoesNotExistException;
import com.escript.exceptions.contextful.UserDoesNotExistException;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;

public class UserService {
    private final AccountDBRepo accountRepo;
    private final FriendshipDBRepo friendshipRepo;
    private final Graph<Long> friendshipGraph;
    private final Argon2 argon2;

    public UserService() throws DuplicateElementException, SQLException {
        //TODO: Actually use the table names

        accountRepo = new AccountDBRepo("Users");
        friendshipRepo = new FriendshipDBRepo("Friendships");

        friendshipGraph = new Graph<>();

        argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

        for (Account a : accountRepo.getAll()) {
            friendshipGraph.addNode(a.getIdentifier());
        }

        try {
            for (IdPair p : friendshipRepo.getIdPairs()) {
                friendshipGraph.addEdge(p.getFirst(), p.getSecond());
            }
        } catch (ID_NotFoundException e) {
            throw new RuntimeException(
                    "Friendship graph is invalid. Couldn't add an edge between two users," +
                    " even though all users were added to the graph.");
        }
    }

    public boolean authenticateAccount(Account submittedAccount) {
        try {
            Account actualAccount = accountRepo.get(submittedAccount.getUser().getUsername());
            return argon2.verify(
                    actualAccount.getPasswordString(),
                    submittedAccount.getPassword());
        } catch (ID_NotFoundException e) {
            return false;
        }
    }

    public Account getAccount(Long id) throws ID_NotFoundException {
        return accountRepo.get(id);
    }

    public Account getAccount(String username) throws ID_NotFoundException{
        return accountRepo.get(username);
    }

    public Friendship getFriendshipByUserIDs(IdPair idPair) throws ID_NotFoundException {
        return friendshipRepo.get(idPair);
    }

    public void addAccount(Account userAccount) throws DuplicateElementException, SQLException {
        String hash = argon2.hash(100, 2048, 1, userAccount.getPassword());
        var processedAccount = new Account(userAccount.getUser(), hash);
        long insertedId = accountRepo.add(processedAccount);
        friendshipGraph.addNode(insertedId);
    }

    public void removeUser(String username) throws ID_NotFoundException, SQLException {
        Long id = accountRepo.get(username).getIdentifier();
        accountRepo.remove(id);
        friendshipGraph.removeNode(id);
        for(var f : friendshipRepo.friendshipsOf(id)) {
            friendshipRepo.remove(f.getFriendship().getIdentifier());
        }
    }

    public void removeUser(Long userId) throws ID_NotFoundException, SQLException {
        accountRepo.remove(userId);
        friendshipGraph.removeNode(userId);
        for(var f : friendshipRepo.friendshipsOf(userId)) {
            friendshipRepo.remove(f.getFriendship().getIdentifier());
        }
    }

    public void markFriends(IdPair idPair) throws UserDoesNotExistException, FriendshipAlreadyRegisteredException,
            SQLException {
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
                            LocalDateTime.now()));
        } catch (DuplicateElementException e) {
            throw new FriendshipAlreadyRegisteredException();
        }
    }

    public void unmarkFriends(IdPair idPair) throws UserDoesNotExistException, FriendshipDoesNotExistException {
        try {
            friendshipGraph.removeEdge(idPair.getFirst(), idPair.getSecond());
        } catch (ID_NotFoundException e) {
            throw new UserDoesNotExistException();
        } catch (EdgeNotFoundException e) {
            String message = "Couldn't unfriend users with IDs %d and %d, " +
                    "because they are not friends in the first place.";
            throw new FriendshipDoesNotExistException(message, e);
        }

        try {
            friendshipRepo.remove(idPair);
        } catch (ID_NotFoundException e) {
            String message = "Friendship removed from graph, but not found in database";
            throw new FriendshipDoesNotExistException(message, e);
        }
    }

    public Collection<Account> getAllUsers() {
        return accountRepo.getAll();
    }

    public Iterable<User> friendsOf(String username) {
        return friendshipRepo.friendsOf(username);
    }

    public Collection<FriendshipDTO> friendshipsOf(Long userId) {
        return friendshipRepo.friendshipsOf(userId);
    }

    public int countCommunities() {
        return friendshipGraph.countConnectedComponents();
    }

//    public Iterable<Account> getMostConnectedCommunity() {
//        var communities = friendshipGraph.getConnectedComponents();
//        ArrayList<Long> longestChain = new ArrayList<>();
//        for(var community : communities){
//            var friendChain = friendshipGraph.longestSimplePath(community);
//            if(longestChain.size() < friendChain.size())
//                longestChain = new ArrayList<>(friendChain);
//        }
//
//        return longestChain.stream().map(this::getAccount).toList();
//    }

    public void resetCurrentStreaks() throws SQLException {
        friendshipRepo.resetAllStreaks();
    }

    public void incrementStreak(IdPair idPair) throws FriendshipDoesNotExistException, SQLException {
        friendshipRepo.incrementStreak(idPair);
    }

    public void closeConnections() throws SQLException {
        accountRepo.closeConnection();
        friendshipRepo.closeConnection();
    }
}
