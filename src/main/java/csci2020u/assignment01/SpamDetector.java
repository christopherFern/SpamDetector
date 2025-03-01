package csci2020u.assignment01;

import javax.annotation.processing.Filer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SpamDetector {
    public HashMap<String, Double> wordProbabilities;

    public SpamDetector() {
        // TODO: Put your File I/O code and spam detection algorithm here
        //initializing a hashmap to hold all the words and the probability that an email is spam if that word appears in an email
        this.wordProbabilities = new HashMap<>();

        //creating File objects and setting them to the ham and spam directories
        File hamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/train/ham")).getFile());
        File spamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/train/spam")).getFile());
        File ham2Folder = new File(Objects.requireNonNull(getClass().getResource("/data/train/ham2")).getFile());

        //creating list of files from the directories above
        File[] hamFiles = hamFolder.listFiles();
        File[] spamFiles = spamFolder.listFiles();
        File[] ham2Files = ham2Folder.listFiles();

        //initializing hashmaps to hold all the words and how many emails they appear in. One for the spam folder and one for the ham folder
        HashMap<String, Integer> trainHamFreq = new HashMap<>();
        HashMap<String, Integer> trainSpamFreq = new HashMap<>();

        //running the functions to populate  the hashmaps just above
        updateWordFrequency(hamFiles, trainHamFreq);
        updateWordFrequency(ham2Files, trainHamFreq);
        updateWordFrequency(spamFiles, trainSpamFreq);

        // running the function to populate wordProbabilities the Hashmap at the very top
        this.wordProbabilities = calculateProbabilities(spamFiles, hamFiles, ham2Files, trainHamFreq, trainSpamFreq);
    }

    //a getter for wordProbabilities
    public HashMap<String, Double> getWordProbabilities() {
        return wordProbabilities;
    }

    //The function that takes an array of files and a HashMap<String, Integer> and populates the
    // hashmap with every word in all the files and how many emails each word appears in.
    public void updateWordFrequency(File[] files, HashMap<String, Integer> freqMap) {
        //checks if the array of files is empty
        if (files != null) {
            //loops over each file
            for (int i = 0; i < files.length; i++) {
                // creating a set of words so that words are not repeated
                Set<String> wordsInCurrentFile = new HashSet<>();
                BufferedReader bufferedReader = null;
                try {
                    FileReader fileReader = new FileReader(files[i]);
                    bufferedReader = new BufferedReader(fileReader);
                    String line;
                    //while loop going over each line in a file
                    while ((line = bufferedReader.readLine()) != null) {
                        //for loop going through each word in a line
                        for (String word : line.split("\\s+")) {
                            //makes all the words lower case so that words like "Joe" and "joe" are considered as the same word.
                            //also only considers words made up of letters in the alphabet to not consider random symbols and numbers
                            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                            //checks if word is empty
                            if (!word.isEmpty()) {
                                //checks is word is already in the set, if it is not, it gets added to the set and updates the frequency map
                                //if not it moves on
                                if (!wordsInCurrentFile.contains(word)) {
                                    wordsInCurrentFile.add(word);
                                    if (freqMap.containsKey(word)) {
                                        freqMap.put(word, freqMap.get(word) + 1);
                                    } else {
                                        freqMap.put(word, 1);
                                    }
                                }
                            }
                        }
                    }

                }
                //error handling
                catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                //closing the file reader
                finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        }
                        catch (IOException e) {
                            System.err.println("Error closing BufferedReader: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    //function that takes the list of spam and ham files and frequency hashmaps and returns a hashmap
    // of all the words and the probability that an email is spam if it includes this word
    public HashMap<String, Double> calculateProbabilities(File[] spamFiles, File[] hamFiles, File[] ham2Files, HashMap<String, Integer> trainHamFreq, HashMap<String, Integer> trainSpamFreq) {

        //initializing the hashmap that will be returned
        HashMap<String, Double> wordProbabilities = new HashMap<>();

        //looping over the words in the spam frequency hashmap
        for (String word : trainSpamFreq.keySet()) {
            //calculating the probability that the current word appears in a spam file
            double P_Wi_S = (trainSpamFreq.get(word)) / (double) (spamFiles.length);
            double P_Wi_H;
            //seeing if the word is in the key set of the Ham frequency hashmap
            //then calculating the probability the current word appears in a Ham file
            if (trainHamFreq.containsKey(word)) {
                P_Wi_H = (trainHamFreq.get(word)) / (double) (hamFiles.length + ham2Files.length);
            } else {
                P_Wi_H = 0.0;
            }

            //calculating the probability that a file is spam because it contains the current word
            double P_S_Wi = (P_Wi_S) / (P_Wi_S + P_Wi_H);
            wordProbabilities.put(word, P_S_Wi);
        }
        return (wordProbabilities);
    }

    public double calculateAccuracy(List<TestFile> results){
        //avoid errors
        if (results == null || results.isEmpty()){
            return 0;
        }
        int correctPredictions = 0;
        //check each file and see if prediction was correct
        for (TestFile file : results) {
            String predictedClass = file.getSpamProbability() > 0.9999999999999999 ? "Spam" : "Ham";
            if (predictedClass.equals(file.getActualClass())) {
                correctPredictions++;
            }
        }
        //calculate and return accuracy
        return (double) correctPredictions/results.size();
    }

    public double calculatePrecision(List<TestFile> results) {
        //avoid errors
        if (results == null || results.isEmpty()) {
            return 0.0;
        }

        int truePositives = 0;
        int falsePositives = 0;

        //check each file for true positives and false positives
        for (TestFile file : results) {
            String predictedClass = file.getSpamProbability() > 0.9999999999999999 ? "Spam" : "Ham";
            if (predictedClass.equals("Spam")) {
                if (predictedClass.equals(file.getActualClass())) {
                    truePositives++;
                } else {
                    falsePositives++;
                }
            }
        }
        //calculate and return precision
        return (double) truePositives / (truePositives + falsePositives);
    }

    public List<TestFile> test(File testHamFolder, File testSpamFolder, HashMap<String, Double> wordProbabilities){
        List<TestFile> results = new ArrayList<>();

        if (testHamFolder != null && testHamFolder.isDirectory()){
            File[] testHamFiles = testHamFolder.listFiles();
            if (testHamFiles != null){
                for (File file : testHamFiles) {
                    double spamProbability = calculateSpamProbability(file, wordProbabilities);
                    results.add(new TestFile(file.getName(), spamProbability, "Ham"));
                }
            }

        }
        if (testSpamFolder != null && testSpamFolder.isDirectory()){
            File[] testSpamFiles = testSpamFolder.listFiles();
            if (testSpamFiles != null) {
                for (File file : testSpamFiles){
                    double spamProbability = calculateSpamProbability(file, wordProbabilities);
                    results.add(new TestFile(file.getName(), spamProbability, "Spam"));
                }
            }
        }
        return results;
    }

    private double calculateSpamProbability(File file, HashMap<String, Double> wordProbabilities){
        double eta = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = reader.readLine()) != null) {
                for (String word : line.split("\\s+")) {
                    word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    if (!word.isEmpty() && wordProbabilities.containsKey(word)) {
                        double pr_S_Wi = wordProbabilities.get(word);
                        eta += Math.log(1 - pr_S_Wi) - Math.log(pr_S_Wi);
                    }
                }
            }
        }catch (IOException e){
            System.err.println("Error " + file.getName());
            e.printStackTrace();
        }
        return(1.0 / (1.0 + Math.exp(eta)));
    }
}