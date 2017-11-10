package org.inlm2.server.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    private static FileHandler instance;
    private List<String> words;
    private static final String FILE = "src/org/inlm2/server/controller/words.txt";

    private FileHandler(){
        words = new ArrayList<>();
        loadWords();
    }

    public List<String> getWords() {
        return words;
    }

    public static FileHandler getInstance() {
        if(instance == null) {
            instance = new FileHandler();
        }
        return instance;
    }

    private void loadWords() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(FILE)));
            String s;
            while((s = br.readLine()) != null) {
                words.add(s);
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
