import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatFilter {
    private ArrayList<String> blacklist;

    public ChatFilter(String badWordsFileName) {
        File file = new File(badWordsFileName);
        blacklist = new ArrayList<>();
        try {
            Scanner in = new Scanner(file);
            while (in.hasNext()) {
                blacklist.add(in.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("No banned words list found.");
        }
    }

    public String filter(String msg) {
        for (String word : blacklist) {
            String censored = "";
            for (int i = 0; i < word.length(); i++) {
                censored += "*";
            }
            msg = msg.replaceAll("(?i)"+word, censored);
        }
        return msg;
    }

    public int numWords() {
        return blacklist.size();
    }

    public String toString() {
        String str = "Banned Words:\n";
        for (String word : blacklist)
            str += word + "\n";
        return str;
    }

//    public static void main(String[] args) {
//        // testing dm
//        String input = "/msg user";
//        try {
//            String recipient = input.substring(5, input.indexOf(" ", 5));
//            System.out.println(recipient);
//            String msg = input.substring(input.indexOf(" ", input.indexOf(" ", 5)) + 1);
//            System.out.println(msg);
//        } catch (Exception e) {
//            System.out.println("Error sending message.");
//        }
//    }
}
