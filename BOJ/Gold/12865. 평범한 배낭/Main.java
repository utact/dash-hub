import java.io.*;
import java.util.*;

/*
 * 첫 줄에 물품의 수 N, 준서가 버틸 수 있는 무게 K
 * 
 * N번 반복하는 반복문
 * ㄴ 물건의 무게 W, 해당 물건의 가치 V
 * 
 * 출력할 것: 물건들의 가치합 최댓값
 */

public class Main {
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		int rep = Integer.parseInt(st.nextToken());
		int idx = Integer.parseInt(st.nextToken());
		
		int[] dp = new int[idx + 1];
		
		for (int i = 0; i < rep; i++) {
			st = new StringTokenizer(br.readLine());
			
			int w = Integer.parseInt(st.nextToken());
			int v = Integer.parseInt(st.nextToken());
			
			for (int j = idx; j >= w; j--) {
                // 기존 가치 혹은 이번에 들어온 물건을 반영한 가치
				dp[j] = Math.max(dp[j], dp[j - w] + v);
			}
		}
		
		System.out.println(dp[idx]);
	}
}