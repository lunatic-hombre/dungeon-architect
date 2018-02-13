package mapgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class CowsNBulls {

    public static void main(String[] args) throws IOException {

        Solution solution = new Solution();
        System.out.println("Solution: "+solution);

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            String guess;

            while ((guess = reader.readLine()) != null) {

                if (!guess.matches("\\d{4}")) {
                    System.out.println("WAT!?");
                    continue;
                }

                int cows=0, bulls=0;
                SolutionSet solutionSet = solution.toSet();

                for (int i = 0; i < guess.length(); i++) {
                    if (guess.charAt(i) == solution.charAt(i))
                        cows++;
                    if (solutionSet.find(guess.charAt(i)) >= 0) {
                        solutionSet.remove(guess.charAt(i));
                        bulls++;
                    }
                }
                System.out.println("Cows "+cows+", Bulls "+(bulls-cows));
            }

        }

    }

    private static class Solution {

        final String string;

        public Solution() {
            final StringBuilder sb = new StringBuilder(4);
            for (int i=0; i < 4; i++)
                sb.append((char)((int)('0')+(int)(9*Math.random())));
            this.string = sb.toString();
        }

        public char charAt(int index) {
            return string.charAt(index);
        }

        SolutionSet toSet() {
            return new SolutionSet(string.toCharArray());
        }

        @Override
        public String toString() {
            return string;
        }

    }

    static class SolutionSet {

        char[] solutionArr;

        SolutionSet(char[] solutionArr) {
            this.solutionArr = solutionArr;
            Arrays.sort(solutionArr);
        }

        void remove(char ch) {
            solutionArr[Arrays.binarySearch(solutionArr, ch)] = ' ';
            Arrays.sort(solutionArr);
        }

        int find(char ch) {
            return Arrays.binarySearch(solutionArr, ch);
        }

    }

}
