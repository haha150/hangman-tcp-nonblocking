package org.inlm2.server.model;

public class Game {

    private String word;
    private int tries;
    private boolean hasWon;
    private char[] wordArray;

    public Game(String word) {
        this.word = word.toLowerCase();
        tries = word.length();
        wordArray = new char[tries];
        for(int i = 0; i < wordArray.length;i++) {
            wordArray[i] = '_';
        }
        hasWon = false;
    }

    public boolean hasWon() {
        return hasWon;
    }

    public int getTries() {
        return tries;
    }

    public boolean isGuessValid(String guess) {
        return guess.length() == word.length() || guess.length() == 1;
    }

    public void guess(String guess) {
        if(guess.length() == word.length()) {
            if(guess.equalsIgnoreCase(word)) {
                solveWord();
                hasWon = true;
            }
        } else  {
            checkAndReplaceChar(guess);
        }
        tries--;
        if (isWordSolved()) {
            hasWon = true;
        }
    }

    @Override
    public String toString() {
        return "Word: " + String.valueOf(wordArray) + ", Remaining failed attempts: " + tries;
    }

    private void checkAndReplaceChar(String guess) {
        for(int i = 0; i < wordArray.length;i++) {
            if(word.charAt(i) == guess.charAt(0)) {
                wordArray[i] = guess.charAt(0);
            }
        }
    }

    private void solveWord() {
        for(int i = 0; i < wordArray.length;i++) {
            wordArray[i] = word.charAt(i);
        }
    }

    private boolean isWordSolved() {
        return String.valueOf(wordArray).equalsIgnoreCase(word);
    }
}
