package hangman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame {

    //All of the possible words
    private Set<String> possible_words = new TreeSet<>();

    //Groups of possible words
    private Map<String, Set<String>> pattern_group_words = new TreeMap();

    private SortedSet<Character> guessed_letters = new TreeSet<>();
    private String right_pattern;
    private int guess_number;
    private char guess_char;

    public EvilHangmanGame() {}

    public EvilHangmanGame(int guess_number) {
        this.guess_number = guess_number;
    }

    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        //Read all the possible words into the possible words set
        read_file_into_possible_words(dictionary, wordLength);

        //Create the initial pattern
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < wordLength; i++) {
            builder.append("-");
        }
        right_pattern = builder.toString();
    }

    public void play_game() {
        boolean game_continue = true;
        boolean win = false;
        do {
            display_game();
            prompt_user_guess(guess_number);

            try {
                possible_words = makeGuess(guess_char);
            } catch (GuessAlreadyMadeException e) {
                System.out.println(e);
                continue;
            }

            if(!right_pattern.contains("-")) {
                win = true;
                break;
            }
            if(right_pattern.contains(Character.toString(guess_char))) {
                System.out.printf("Yes, there is one or more %c\n\n", guess_char);
            } else {
                System.out.printf("Sorry, there are no %c's\n\n", guess_char);
            }
            //Everytime we will decrease one guess time
            guess_number--;

            if(guess_number == 0) {
                break;
            }

        } while(game_continue);

        if(win) {
            System.out.println("You win!");
        } else {
            System.out.println("You lose!");
            System.out.printf("The word was: %s", possible_words.iterator().next());
        }
    }
    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        guess_char = Character.toLowerCase(guess);
        pattern_group_words.clear();

        if (guessed_letters.contains(guess_char)) {
            throw new GuessAlreadyMadeException("You already used that letter");
        } else {
            guessed_letters.add(guess_char);
        }

        String pattern = "";
        for(String word : possible_words) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < word.length(); i++){
                if(guess_char == word.charAt(i)) {
                    builder.append(guess_char);
                } else {
                    builder.append('-');
                }
            }

            pattern = builder.toString();

            //If the pattern exists
            if(pattern_group_words.containsKey(pattern)) {
                pattern_group_words.get(pattern).add(word);
            } else {
                Set<String> word_group = new TreeSet<>();
                word_group.add(word);
                pattern_group_words.put(pattern, word_group);
            }
        }
        //Choose which group of words to replace the original dictionary
        possible_words = find_largest_group();

        return possible_words;
    }
    private Set<String> find_largest_group() {
        String right_key = "";
        Set<String> largest_group = new TreeSet<>();
        for(String key : pattern_group_words.keySet()) {
            if (pattern_group_words.get(key).size() > largest_group.size()) {
                right_key = key;
                largest_group = pattern_group_words.get(right_key);
            }
            if (pattern_group_words.get(key).size() == largest_group.size()) {
                String temp_result_key = diff(right_key, key);
                if(temp_result_key != null) {
                    right_key = temp_result_key;
                } else if(right_key.compareTo(key) > 0) {
                    right_key = key;
                }
            }
        }

        StringBuilder builder = new StringBuilder(right_pattern);
        for(int i = 0; i < right_key.length(); i++) {
            if(right_key.charAt(i) != '-') {
                builder.setCharAt(i, right_key.charAt(i));
            }
        }
        right_pattern = builder.toString();


        return pattern_group_words.get(right_key);
    }

    //Compare with how many dashed
    private String diff(String right_key, String key) {
        int count = 0;
        int count2 = 0;
        for(int i = 0; i < right_key.length(); i ++) {
            if (right_key.charAt(i) == '-') {
                count++;
            }
            if (key.charAt(i) == '-') {
                count2++;
            }
        }

        if(count > count2) {
            return right_key;
        }
        if(count < count2) {
            return key;
        }
        return null;
    }



    private void read_file_into_possible_words(File dictionary, int word_length) throws FileNotFoundException, EmptyDictionaryException {

        //If the file is empty
        if(dictionary.length() == 0) {
            throw new EmptyDictionaryException("The dictionary is empty");
        } else if (word_length == 0) {
            throw new EmptyDictionaryException("Word length should be greater than 1");
        }

        Scanner scanner = new Scanner(dictionary);
        //Every time we play the game, we have to clear out the previous data in possible word set
        possible_words.clear();

        while(scanner.hasNext()){
            String word = scanner.next();
            if(word.length() == word_length) {
                possible_words.add(word);
            }
        }

        //If no words in the dictionary match the word length
        if(possible_words.size() == 0){
            throw new EmptyDictionaryException("No words in the dictionary match the word length");
        }
    }
    private void display_game() {
        System.out.printf("You have %d guesses left\n", guess_number);
        System.out.printf("Used letters: ");
        for(char letter : guessed_letters) {
            System.out.printf("%c ", letter);
        }
        System.out.printf("\nWord: %s\n", right_pattern);
    }

    private void prompt_user_guess(int guess_number) {
        Scanner scanner = new Scanner(System.in);

        boolean valid = false;
        do  {
            System.out.print("Enter guess: ");
            guess_char = scanner.next().charAt(0);

            //If it not a letter
            if(!Character.isLetter(guess_char) || Character.isWhitespace(guess_char)) {
                System.out.println("Invalid input");
                continue;
            }

            valid = true;

        } while (!valid);

    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return guessed_letters;
    }



    //          Set<String> largest_group = new TreeSet<>();
//        Map<String, Set<String>> other_largest_groups = new TreeMap<>();

//        boolean found = false;
//        for (Map.Entry<String, Set<String>>group : pattern_group_words.entrySet()) {
//
//            if (group.getValue().size() > largest_group.size()) {
//                largest_group = group.getValue();
//                other_largest_groups.put(group.getKey(), group.getValue());
//            } else if (group.getValue().size() == largest_group.size()) {
//                other_largest_groups.put(group.getKey(), group.getValue());
//            }
//        }
//
//
//        StringBuilder build_empty_key = new StringBuilder();
//        for(int i = 0; i < pattern.length(); i++) {
//            build_empty_key.append("-");
//        }
//        String empty_key = build_empty_key.toString();
//
//        int fewest_letters = 1000;
//
//        //two or more groups are of the same size
//        if(other_largest_groups.size() > 1) {
//
//            if(other_largest_groups.containsKey(empty_key)) {
//                return other_largest_groups.get(empty_key);
//            }

    //If we choose the one with the fewest letters
    //    String smallest_key = "";
//            Set<String> key_set = new TreeSet<>();
//            for(String key : other_largest_groups.keySet()) {
//                int letter = 0;
//                for(int i = 0; i < key.length(); i++) {
//                    if(guess_char == key.charAt(i)) {
//                        letter++;
//                    }
//                }
//                if(letter < fewest_letters) {
//                    fewest_letters = letter;
//                    smallest_key = key;
//                    key_set.add(smallest_key);
//                }
//            }
//
//            if(key_set.size() <= 1) {
//                return other_largest_groups.get(smallest_key);
//            } else {
//                for(String key : other_largest_groups.keySet()) {
//                    for(String key2 : key_set) {
//                        if(key2 == key) {
//                            other_largest_groups.remove(key2);
//                        }
//                    }
//                }
//            }
//            for(Map.Entry<String, Set<String>> group : other_largest_groups.entrySet()){
//                int letter = 0;
//                //Loop through the string and find the guess letter
//                for(int i = 0; i < group.getKey().length(); i++){
//
//                    if (guess_char == group.getKey().charAt(i)) {
//                        letter++;
//                    }
//                }
//
////                if(letter < fewest_letters) {
////                    fewest_letters = letter;
////                    smallest_key = group.getKey();
////                }
//            }

//            if(other_largest_groups.size() <= 1) {
//                return other_largest_groups.get(smallest_key);
//            }

    //Choose the one with the rightmost guessed letter
//            for(Map.Entry<String, Set<String>> group : other_largest_groups.entrySet()) {
//                //Lopp through each character
//                for (int i = group.getKey().length() - 1; i >= 0 ; i--) {
//                    if(group.getKey().charAt(i) == guess_char) {
//                        return group.getValue();
//                    }
//                }
//            }
//
//            while(other_largest_groups.size() != 1) {
//                for (Map.Entry<String, Set<String>> group : other_largest_groups.entrySet()) {
//                    for (int i = group.getKey().length() - 1; i >= 0 ; i--) {
//                        if(Character.isLetter(group.getKey().charAt(i))) {
//                            return group.getValue();
//                        }
//                    }
//                }
//            }
//        }
}
