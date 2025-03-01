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
        this.wordProbabilities = new HashMap<>();
        File hamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/train/ham")).getFile());
        File spamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/train/spam")).getFile());
        File ham2Folder = new File(Objects.requireNonNull(getClass().getResource("/data/train/ham2")).getFile());

        File[] hamFiles = hamFolder.listFiles();
        File[] spamFiles = spamFolder.listFiles();
        File[] ham2Files = ham2Folder.listFiles();

        File testHamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/test/ham")).getFile());
        File testSpamFolder = new File(Objects.requireNonNull(getClass().getResource("/data/test/spam")).getFile());


        HashMap<String, Integer> trainHamFreq = new HashMap<>();
        HashMap<String, Integer> trainSpamFreq = new HashMap<>();



        updateWordFrequency(hamFiles, trainHamFreq);
        updateWordFrequency(ham2Files, trainHamFreq);
        updateWordFrequency(spamFiles, trainSpamFreq);

        this.wordProbabilities = calculateProbabilities(spamFiles, hamFiles, ham2Files, trainHamFreq, trainSpamFreq);


    }
    public HashMap<String, Double> getWordProbabilities() {
        return wordProbabilities;
    }

    public void updateWordFrequency(File[] files, HashMap<String, Integer> freqMap) {
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                Set<String> wordsInCurrentFile = new HashSet<>();
                BufferedReader bufferedReader = null;
                try {
                    FileReader fileReader = new FileReader(files[i]);
                    bufferedReader = new BufferedReader(fileReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        for (String word : line.split("\\s+")) {
                            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                            if (!word.isEmpty()) {
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
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            System.err.println("Error closing BufferedReader: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public HashMap<String, Double> calculateProbabilities(File[] spamFiles, File[] hamFiles, File[] ham2Files, HashMap<String, Integer> trainHamFreq, HashMap<String, Integer> trainSpamFreq) {
        int totalSpamFiles = spamFiles.length;
        int totalHamFiles = hamFiles.length + ham2Files.length;

        System.out.println("Size of trainSpamFreq: " + trainSpamFreq.size());

        HashMap<String, Double> wordProbabilities = new HashMap<>();
        for (String word : trainSpamFreq.keySet()) {
            double P_Wi_S = (trainSpamFreq.get(word) + 1) / (double) (totalSpamFiles+1);
            double P_Wi_H;
            if (trainHamFreq.containsKey(word)) {
                P_Wi_H = (trainHamFreq.get(word) + 1) / (double) (totalHamFiles+1);
            } else {
                P_Wi_H = 0.0;
            }


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
//
//    public static void main(String[] args) {
//        new SpamDetector();
//    }
}

