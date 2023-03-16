package com.escript.web;
//
//import com.escript.domain.Account;
//import com.escript.domain.User;
//import com.escript.exceptions.DuplicateElementException;
//import com.escript.exceptions.ID_NotFoundException;
//import com.escript.service.UserService;
//import com.google.gson.Gson;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;

//@SpringBootApplication
//@RestController
//public class RESTController {
//    UserService userService;
//    Gson gson;
//
//    public RESTController() {
//        this.userService = new UserService();
//        this.gson = new Gson();
//    }
//
//    @PutMapping("/api/create-account")
//    private void createAccount(@RequestParam String username, @RequestParam String password) {
//        var userAccount = new Account(new User(username), password);
//        try {
//            userService.addAccount(userAccount);
//        } catch (DuplicateElementException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @DeleteMapping("/api/delete-account")
//    private void deleteAccount(@RequestParam String username, @RequestParam String password) {
//        var userAccount = new Account(new User(username), password);
//        if(userService.authenticateAccount(userAccount)) {
//            userService.removeUser(userAccount);
//        } else {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
//        }
//    }
//
//    @GetMapping("/api/userinfo")
//    private User getUserDetails(@RequestParam String targetUsername) {
//        try {
//            Account userAccount = userService.getAccount(targetUsername);
//            return userAccount.getUser();
//        } catch (ID_NotFoundException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @PutMapping("/api/friends/request")
//    private void makeFriendRequest(@RequestParam String ownUsername,
//                              @RequestParam String ownPassword,
//                              @RequestParam String targetUsername)
//    {
//
//    }
//
//    @PutMapping("/api/friends/accept")
//    private void acceptFriend(@RequestParam String ownUsername, @RequestParam String ownPassword, @RequestParam String targetUsername)
//    {
//
//    }
//
//    @GetMapping("/api/friends/pending")
//    private List<User> getPendingFriendRequests(@RequestParam String ownUsername, @RequestParam String ownPassword)
//    {
//        var account = new Account(new User(ownUsername), ownPassword);
//        return null;
////        if (userService.authenticateAccount(account)){
////
////        } else {
////
////        }
//    }
//
//    @GetMapping("/api/friends/of")
//    private Iterable<User> getFriendsOf(@RequestParam String targetUsername) {
//        return userService.friendsOf(targetUsername);
//    }
//
////    @GetMapping("/api/users")
////    private Collection<User> getUsers() {
//////        return gson.toJson(userService.getAllUsers());
////        var reply = userService.getAllUsers();
////        for (User u : reply) {
////        }
////    }
//}
