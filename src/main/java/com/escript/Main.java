package com.escript;

import com.escript.user_interface.TextUI;

public class Main {
    public static void main(String[] args) {
        var textUI =  TextUI.getInstance();
        textUI.displayMenu();

        while(true) {
            String input = textUI.prompt();
            textUI.parseInput(input);
        }
    }
}