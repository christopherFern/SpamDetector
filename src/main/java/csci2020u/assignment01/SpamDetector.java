package csci2020u.assignment01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SpamDetector {

    public SpamDetector() {
        // TODO: Put your File I/O code and spam detection algorithm here
        File hamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/train/ham")).getFile());
        File spamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/train/spam")).getFile());

        File[] hamFiles = hamFolder.listFiles();
        File[] spamFiles = spamFolder.listFiles();

        HashMap<String, Integer> trainHamFreq = new HashMap<>();
        HashMap<String, Integer> trainSpamFreq = new HashMap<>();



        if (spamFiles != null && hamFiles != null) {

            for (int i = 0; i < hamFiles.length; i++) {
                Set<String> wordsInCurrentFile = new HashSet<>();
                try {
                    FileReader fileReader = new FileReader(hamFiles[i]);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        for (String word : line.split("\\s+")) {
                            word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                            if (!word.isEmpty()) {
                                if(!wordsInCurrentFile.contains(word)) {
                                    wordsInCurrentFile.add(word);
                                    if (trainHamFreq.containsKey(word)) {
                                        trainHamFreq.put(word, trainHamFreq.get(word) + 1);
                                    } else {
                                        trainHamFreq.put(word, 1);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            System.out.println(trainHamFreq);
        }
    }

    public static void main(String[] args) {
        new SpamDetector();
    }
}
