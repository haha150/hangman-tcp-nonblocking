package org.inlm2.server.controller;

import java.util.Random;

public class Controller {

    private static FileHandler fileHandler;
    private static Random random;

    public Controller() {
        fileHandler = FileHandler.getInstance();
        random = new Random();
    }

    public String getRandomWord() {
        return fileHandler.getWords().get(random.nextInt(fileHandler.getWords().size()));
    }
}
