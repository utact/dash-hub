import java.io.*;

public class Solution {

    static StringBuilder sb = new StringBuilder();

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int tc = Integer.parseInt(br.readLine());
        for (int t = 1; t <= tc; t++) {
            int n = Integer.parseInt(br.readLine());
            sb.append("#").append(t).append(" ").append(getFinalSheepNumber(n)).append("\n");
        }

        System.out.println(sb);
    }

    static int getFinalSheepNumber(int n) {
        boolean[] sheep = new boolean[10];
        int cnt = 0;
        int mul = 1;

        while (cnt != 10) {
            int cur = n * mul;
            while (cur != 0) {
                int digit = cur % 10;
                if (!sheep[digit]) {
                    sheep[digit] = true;
                    cnt++;
                }
                cur /= 10;
            }
            mul++;
        }

        return n * (mul - 1);
    }

}