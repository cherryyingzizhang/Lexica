package net.cherryzhang.lexicav1;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Functions {

	public static boolean isMadeOfSpaces(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != ' ') {
				return false;
			}
		}
		return true;

	}

	@SuppressWarnings("null")
	public static ArrayList<String> scrambleAndShuffle(final String currWord) {
		ArrayList<String> words = new ArrayList<String>();
		char[] charArray;
		int strLength = currWord.length();
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		Random r = new Random();

		// if the word is short
		if (strLength < 3) {
			if (strLength == 1) {
				for (int i = 0; i < 3; i++)
				{
					charArray = currWord.toCharArray();
					String newWord = new String(charArray);

					while (newWord.contentEquals(currWord)) {
						charArray[0] = alphabet.charAt(r.nextInt(26));
						newWord = new String(charArray);
					}

					words.add(newWord);
				}
			} else if (strLength == 2) {
                for (int i = 0; i < 3; i++) {
                    charArray = currWord.toCharArray();
                    String newWord = new String(charArray);

                    while (newWord.contentEquals(currWord)) {
                        if (i < 2)
                            charArray[i] = alphabet.charAt(r.nextInt(26));
                        else
                            charArray[1] = alphabet.charAt(r.nextInt(26));
                        newWord = new String(charArray);
                    }

                    words.add(newWord);
                }
			}
		} else {
			for (int i = strLength - 1; i > strLength - 4; i--) {
				charArray = currWord.toCharArray();
				String newWord = new String(charArray);

				while (newWord.contentEquals(currWord)) {
					charArray[i] = alphabet.charAt(r.nextInt(26));
					newWord = new String(charArray);
				}

				words.add(newWord);

			}
		}

		words.add(currWord);

		Log.w("", words.get(0));
		Log.w("", words.get(1));
		Log.w("", words.get(2));
		Log.w("", words.get(3));

		Collections.shuffle(words);

		return words;
	}

}
