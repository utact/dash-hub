import java.io.*;
import java.util.*;

public class Solution {
	static int[] dr = { -1, -1, -1, 0, 0, 1, 1, 1 };
	static int[] dc = { -1, 0, 1, -1, 1, -1, 0, 1 };
	static int N, ans;
	static int[][] ips, map, vst;

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int T = Integer.parseInt(br.readLine());

		StringBuilder sb = new StringBuilder();

		for (int tc = 1; tc <= T; tc++) {
			N = Integer.parseInt(br.readLine());
			ans = N * N;
			ips = new int[N][N];
			map = new int[N][N];
			vst = new int[N][N];

			for (int r = 0; r < N; r++) {
				String line = br.readLine();
				for (int c = 0; c < N; c++) {
					ips[r][c] = line.charAt(c);
				}
			}

			toNum();
			game();

			sb.append('#').append(tc).append(' ').append(ans).append('\n');
		}
		
		System.out.print(sb);
	}

	static void toNum() {
		for (int r = 0; r < N; r++) {
			for (int c = 0; c < N; c++) {
				if (ips[r][c] != '.') {
					vst[r][c] = 1;
					ans--;
					for (int i = 0; i < 8; i++) {
						int nr = r + dr[i];
						int nc = c + dc[i];

						if (nr < 0 || nr >= N || nc < 0 || nc >= N) {
							continue;
						}

						map[nr][nc]++;
					}
				}
			}
		}
	}

	static void game() {
		Queue<int[]> q = new ArrayDeque<int[]>();

		for (int r = 0; r < N; r++) {
			for (int c = 0; c < N; c++) {
				if (vst[r][c] == 0 && map[r][c] == 0) {
					q.add(new int[] {r, c});
					
					while (!q.isEmpty()) {
						int[] cur = q.remove();
						int cr = cur[0];
						int cc = cur[1];
						
						vst[r][c] = 1;
						
						for (int i = 0; i < 8; i++) {
							int nr = cr + dr[i];
							int nc = cc + dc[i];
							
							if (nr < 0 || nr >= N || nc < 0 || nc >= N || vst[nr][nc] == 1) {
								continue;
							}
							
							if (map[nr][nc] == 0) {
								q.add(new int[] {nr, nc});
							}
							
							vst[nr][nc] = 1;
							
							ans--;
						}
					}
				}
			}
		}
		
	}
	
	static void printMap(int[][] map) {
		for (int r = 0; r < map.length; r++) {
			for (int c = 0; c < map.length; c++) {
				System.out.print(map[r][c] + " ");
			}
			System.out.println();
		}
	}
}
