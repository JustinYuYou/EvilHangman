package hangman;

import java.io.File;
import java.io.IOException;

public class EvilHangman {
    public static void main(String[] args) throws IOException, EmptyDictionaryException {
        try {
            if(args.length != 3) {
                throw new Exception("There should be 3 arguments");
            }

        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please type: java EvilHangman.java <file.txt> <word length> <number of guess>");
            System.exit(0);
        }

        String file_name = args[0];
        int word_length = Integer.parseInt(args[1]);
        int guess_number = Integer.parseInt(args[2]);
        File dictionary_file = new File(file_name);

        try {
            if(word_length < 2){
                throw new Exception("Word length should be at least 2");
            } else if (word_length > 12) {
                throw new Exception("Word length should not be over 12");
            } else if (guess_number < 1) {
                throw new Exception("Number of guess should be at least 1");
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }

        //Set up the game
        EvilHangmanGame evilHangmanGame = new EvilHangmanGame(guess_number);
        try {
            evilHangmanGame.startGame(dictionary_file, word_length);
        } catch (EmptyDictionaryException e) {
            System.out.println(e);
            System.exit(0);
        }
        //Play game
        evilHangmanGame.play_game();
    }

}

