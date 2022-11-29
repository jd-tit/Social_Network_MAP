package com.escript.user_interface;

import com.escript.ctrl.UserService;
import com.escript.data.IdPair;
import com.escript.domain.User;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipAlreadyRegisteredException;
import com.escript.exceptions.contextful.FriendshipDoesNotExistException;
import com.escript.exceptions.contextful.UserDoesNotExistException;

import java.util.Scanner;

/**
 * Handle UI tasks using a text interface
 */
public class TextUI {
    private static TextUI instance;
    private final UserService userService;
    private TextUI(){
        this.scanner = new Scanner(System.in);
        this.userService = new UserService();
    }
    private final Scanner scanner;
    private final TextMenuItem[] items = {
            new TextMenuItem("help", "Display this menu"),
            new TextMenuItem("exit", "Quit the program"),
            new TextMenuItem("au", "Create a new user"),
            new TextMenuItem("ru", "Remove an user"),
            new TextMenuItem("fr", "Mark two users as friends"),
            new TextMenuItem("ufr", "Unmark two users as friends"),
            new TextMenuItem("groups", "Count how many communities there are in the network"),
            new TextMenuItem("lu", "List all users"),
            new TextMenuItem("lf", "List all friendships"),
            new TextMenuItem("mcc", "List the members of the most connected community in our network"),
            new TextMenuItem("rs", "Reset all current streaks, as if it were a new day"),
            new TextMenuItem("is", "Increment two user's messaging streak")
    };

    /**
     * Get the instance of the class' singleton
     */
    public static TextUI getInstance(){
        if(TextUI.instance == null)
            TextUI.instance = new TextUI();
        return instance;
    }


    public void displayMenu(){
        System.out.println("Options:");
        System.out.printf("%17s - %s%n", "Command", "Description");
        for (TextMenuItem item : this.items) {
            System.out.printf("<%15s> - %s%n", item.shortcut(), item.description());
        }
        System.out.println();
    }

    public String prompt(){
        System.out.print("What do you want to do? --> ");
        return scanner.nextLine();
    }

    public void parseInput(String input) {
        input = input.strip();
        switch(input) {
            case "help" -> displayMenu();
            case "exit" -> System.exit(0);
            case "add user", "au" -> addUser();
            case "remove user", "ru" -> removeUser();
            case "friend", "fr" -> markAsFriends();
            case "unfriend", "ufr" -> unmarkAsFriends();
            case "groups" -> countCommunities();
            case "lu" -> listAllUsers();
            case "lf" -> listAllFriendships();
            case "mcc" -> mostConnectedCommunity();
            case "rs" -> resetCurrentStreaks();
            case "is" -> incrementStreak();
            default -> commandNotFound(input);
        }
    }

    private void resetCurrentStreaks() {
        userService.resetCurrentStreaks();
        System.out.println("All current streaks have been reset");
    }

    private void incrementStreak() {
        System.out.println("ID of user 1:");
        var id1 = scanner.nextLong();
        System.out.println("ID of user 2:");
        var id2 = scanner.nextLong();
        scanner.nextLine(); //skip forward
        IdPair idPair;

        try {
            idPair = new IdPair(id1, id2);
        } catch (DuplicateElementException e) {
            System.err.println("The IDs are equal!");
            return;
        }

        try {
            userService.incrementStreak(idPair);
        } catch (UserDoesNotExistException e) {
            if(userService.getUserByID(id1) == null)
                System.err.printf("There is no user with an ID equal to <%d>.%n", id1);
            if(userService.getUserByID(id2) == null)
                System.err.printf("There is no user with an ID equal to <%d>.%n", id2);
        }
        System.out.printf(
                "<%s> and <%s> have now been chatting for %d days!%n",
                userService.getUserByID(id1).getUsername(),
                userService.getUserByID(id2).getUsername(),
                userService.getFriendshipByUserIDs(idPair).getMessagingStreak()
        );
    }

    private void addUser(){
        System.out.println("Choose an username for the new user");
        var username = scanner.nextLine();
        username = username.trim();

        try {
            userService.addUser(new User(username));
        } catch (DuplicateElementException e) {
            System.err.printf("An user with the username <%s> already exists.%n", username);
            return;
        }
        System.out.printf("User <%s> added successfully!%n", username);
    }

    private void removeUser(){
        System.out.println("What is the ID of the user to be deleted?");
        var id = scanner.nextLong();
        scanner.nextLine(); //skip forward

        try{
            userService.removeUser(id);
        } catch (ID_NotFoundException e) {
            System.err.printf("There is no user with an ID equal to <%d>.%n", id);
            return;
        }

        System.out.printf("User with ID <%d> deleted successfully!%n", id);
    }

    private void markAsFriends() {
        System.out.println("ID of user 1:");
        var id1 = scanner.nextLong();
        System.out.println("ID of user 2:");
        var id2 = scanner.nextLong();
        IdPair idPair;
        scanner.nextLine(); //skip forward

        try {
            idPair = new IdPair(id1, id2);
        } catch (DuplicateElementException e) {
            System.err.println("The IDs are equal!");
            return;
        }

        try{
            userService.markFriends(idPair);
        } catch (FriendshipAlreadyRegisteredException e) {
            System.err.printf("The users  \"%s\" and \"%s\" already are friends%n",
                    userService.getUserByID(id1),
                    userService.getUserByID(id2));
            return;
        } catch (UserDoesNotExistException e) {
            if(userService.getUserByID(id1) == null)
                System.err.printf("There is no user with an ID equal to <%d>.%n", id1);
            if(userService.getUserByID(id2) == null)
                System.err.printf("There is no user with an ID equal to <%d>.%n", id2);
            return;
        } catch (DuplicateElementException e) {
            System.err.println("The given user IDs are identical");
            return;
        }
        System.out.printf(
                "<%s> and <%s> are now friends!%n",
                userService.getUserByID(id1).getUsername(),
                userService.getUserByID(id2).getUsername()
        );
    }

    private void unmarkAsFriends() {
        System.out.println("ID of user 1:");
        var id1 = scanner.nextLong();
        System.out.println("ID of user 2:");
        var id2 = scanner.nextLong();
        IdPair idPair;
        scanner.nextLine(); //skip forward

        try {
            idPair = new IdPair(id1, id2);
        } catch (DuplicateElementException e) {
            System.err.println("The IDs are equal!");
            return;
        }

        try{
            userService.unmarkFriends(idPair);
        } catch (UserDoesNotExistException e) {
            if(userService.getUserByID(id1) == null)
                System.err.printf("There is no user with an ID equal to <%d>.%n", id1);
            if(userService.getUserByID(id2) == null)
                System.err.printf("There is no user with an ID equal to <%d>.%n", id2);
            return;
        } catch (FriendshipDoesNotExistException e) {
            System.out.printf(
                    "The users <%s> and <%s> are not friends.",
                    userService.getUserByID(id1).getUsername(),
                    userService.getUserByID(id2).getUsername()
            );
            return;
        } catch (DuplicateElementException e) {
            System.err.println("The given user IDs are identical");
            return;
        }

        System.out.printf(
                "The users <%s> and <%s> are not friends anymore.",
                userService.getUserByID(id1).getUsername(),
                userService.getUserByID(id2).getUsername()
        );
    }

    private void listAllUsers() {
        var users = userService.getAllUsers();
        if(users.isEmpty()){
            System.out.println("There are currently no users.");
            return;
        }
        System.out.println("Here are all of our users:");
        for(var user : users){
            System.out.println(user.toString());
        }
    }

    private void listAllFriendships() {
        var friendships = userService.getAllFriendships();
        if(friendships.isEmpty()){
            System.out.println("There are currently no friendships.");
            return;
        }
        System.out.println("Here are all of the friendships:");
        for(var friendship : friendships) {
            System.out.printf("%s and %s have been friends since %s%n".formatted(
                    userService.getUserByID(friendship.getUserID1()).getUsername(),
                    userService.getUserByID(friendship.getUserID2()).getUsername(),
                    friendship.getFriendsSince().toString()
            ));
            System.out.printf("Their current message streak is %d and their longest was %d"
                    .formatted(
                            friendship.getMessagingStreak(),
                            friendship.getLongestMessagingStreak())
            );
            System.out.printf("%n%n");
        }
    }

    private void countCommunities() {
        System.out.printf("Currently there are %d communities.%n", userService.countCommunities());
    }

    private void mostConnectedCommunity() {
        System.out.println("The most connected community contains the following people:");
        userService.getMostConnectedCommunity().forEach(System.out::println);
    }

    private void commandNotFound(String notFound){
        System.out.printf("The command \"%s\" could not be found. Please try something else.%n", notFound);
    }
}
