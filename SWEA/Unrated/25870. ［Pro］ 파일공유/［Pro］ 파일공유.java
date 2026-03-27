import java.util.*;

class UserSolution {
    private static final int INF = 5001; // 5000 초과는 다운로드 불가이므로 5001을 INF로 사용
    private static final int MAXN = 1001;
    private static final int MAXF = 505;

    int N;
    int[][] dist = new int[MAXN][MAXN];
    
    // 파일 정보 관리
    Map<Integer, Integer> fileIdMap = new HashMap<>();
    int fileTypeCnt;
    int[] fileSize = new int[MAXF];
    ArrayList<Integer>[] nodesWithFile = new ArrayList[MAXF];
    boolean[][] hasFile = new boolean[MAXN][MAXF];

    // 다운로드 정보 관리
    class Download {
        int reqNode, fileIdx, totalSize;
        long downloaded;
        int lastTime;
        int activeSources;
        boolean isFinished;
    }

    ArrayList<Download> activeList = new ArrayList<>();
    Download[][] nodeDownloadMap = new Download[MAXN][MAXF];

    // Custom Min-Heap (다익스트라용 - GC 방지)
    int[] hNode = new int[100000];
    int[] hDist = new int[100000];
    int hSize;

    private void push(int node, int d) {
        int curr = ++hSize;
        while (curr > 1 && hDist[curr / 2] > d) {
            hNode[curr] = hNode[curr / 2];
            hDist[curr] = hDist[curr / 2];
            curr /= 2;
        }
        hNode[curr] = node;
        hDist[curr] = d;
    }

    private int[] pop() {
        int retNode = hNode[1];
        int retDist = hDist[1];
        int lastNode = hNode[hSize];
        int lastDist = hDist[hSize--];
        
        int curr = 1;
        while (curr * 2 <= hSize) {
            int child = curr * 2;
            if (child + 1 <= hSize && hDist[child + 1] < hDist[child]) {
                child++;
            }
            if (lastDist <= hDist[child]) break;
            hNode[curr] = hNode[child];
            hDist[curr] = hDist[child];
            curr = child;
        }
        hNode[curr] = lastNode;
        hDist[curr] = lastDist;
        return new int[]{retNode, retDist}; // 크기 2 배열 생성은 미미한 수준
    }

    // 인접 리스트 배열 (초기 망 구축용)
    int[] head = new int[MAXN];
    int[] to = new int[4005];
    int[] weight = new int[4005];
    int[] next = new int[4005];
    int edgeCnt;

    private void addEdge(int u, int v, int w) {
        to[++edgeCnt] = v;
        weight[edgeCnt] = w;
        next[edgeCnt] = head[u];
        head[u] = edgeCnt;
    }

    public void init(int N, int[] mShareFileCnt, int[][] mFileID, int[][] mFileSize) {
        this.N = N;
        this.fileTypeCnt = 0;
        this.fileIdMap.clear();
        this.activeList.clear();
        this.edgeCnt = 0;

        for (int i = 1; i <= N; i++) {
            head[i] = 0;
            for (int j = 1; j <= N; j++) dist[i][j] = INF;
            dist[i][i] = 0;
            for (int f = 0; f < MAXF; f++) {
                hasFile[i][f] = false;
                nodeDownloadMap[i][f] = null;
            }
        }

        for (int i = 0; i < MAXF; i++) {
            if (nodesWithFile[i] == null) nodesWithFile[i] = new ArrayList<>();
            else nodesWithFile[i].clear();
        }

        for (int i = 0; i < N; i++) {
            int u = i + 1;
            for (int k = 0; k < mShareFileCnt[i]; k++) {
                int fIdx = getFileIdx(mFileID[i][k]);
                fileSize[fIdx] = mFileSize[i][k];
                nodesWithFile[fIdx].add(u);
                hasFile[u][fIdx] = true;
            }
        }
    }

    private int getFileIdx(int mFileID) {
        if (!fileIdMap.containsKey(mFileID)) {
            fileIdMap.put(mFileID, fileTypeCnt++);
        }
        return fileIdMap.get(mFileID);
    }

    public void makeNet(int K, int[] mComA, int[] mComB, int[] mDis) {
        // 1. 인접 리스트 구성
        for (int i = 0; i < K; i++) {
            int u = mComA[i], v = mComB[i], w = mDis[i];
            addEdge(u, v, w);
            addEdge(v, u, w);
        }

        // 2. N번의 커스텀 다익스트라 수행 (Floyd-Warshall 대체, 5000 초과 하드 가지치기)
        for (int start = 1; start <= N; start++) {
            hSize = 0;
            push(start, 0);

            while (hSize > 0) {
                int[] curr = pop();
                int u = curr[0];
                int d = curr[1];

                if (dist[start][u] < d) continue;

                for (int e = head[u]; e != 0; e = next[e]) {
                    int v = to[e];
                    int w = weight[e];
                    int nextDist = d + w;

                    // 거리 5000 제한 최적화: 5000 이내일 때만 갱신 및 탐색
                    if (nextDist <= 5000 && nextDist < dist[start][v]) {
                        dist[start][v] = nextDist;
                        push(v, nextDist);
                    }
                }
            }
        }
    }

    private void updateDownloads(int mTime) {
        for (int i = activeList.size() - 1; i >= 0; i--) {
            Download d = activeList.get(i);
            if (d.isFinished) continue;

            long duration = mTime - d.lastTime;
            if (duration > 0 && d.activeSources > 0) {
                d.downloaded += duration * 9 * d.activeSources;
                if (d.downloaded >= (long) d.totalSize) {
                    d.downloaded = d.totalSize;
                    d.isFinished = true;
                }
            }
            d.lastTime = mTime;
        }
    }

    private void refreshActiveSources() {
        for (Download d : activeList) {
            if (d.isFinished) continue;
            int count = 0;
            int[] rowReq = dist[d.reqNode];
            for (int sourceNode : nodesWithFile[d.fileIdx]) {
                if (rowReq[sourceNode] <= 5000) count++;
            }
            d.activeSources = count;
        }
    }

    public void addLink(int mTime, int mComA, int mComB, int mDis) {
        updateDownloads(mTime);
        if (mDis >= dist[mComA][mComB]) return;

        dist[mComA][mComB] = dist[mComB][mComA] = mDis;
        
        // 3. 추가 간선 발생 시 전체 탐색 대신 O(N^2) 부분 갱신 수행
        for (int i = 1; i <= N; i++) {
            int[] rowI = dist[i];
            int d_ia = rowI[mComA];
            int d_ib = rowI[mComB];
             
            if (d_ia + mDis < d_ib) {
                int new_d_ib = d_ia + mDis;
                rowI[mComB] = dist[mComB][i] = new_d_ib;
                int[] rowB = dist[mComB];
                for (int j = 1; j <= N; j++) {
                    int potential = new_d_ib + rowB[j];
                    if (potential < rowI[j]) rowI[j] = dist[j][i] = potential;
                }
            } 
            else if (d_ib + mDis < d_ia) {
                int new_d_ia = d_ib + mDis;
                rowI[mComA] = dist[mComA][i] = new_d_ia;
                int[] rowA = dist[mComA];
                for (int j = 1; j <= N; j++) {
                    int potential = new_d_ia + rowA[j];
                    if (potential < rowI[j]) rowI[j] = dist[j][i] = potential;
                }
            }
        }
        refreshActiveSources();
    }

    public void addShareFile(int mTime, int mComA, int mFileID, int mSize) {
        updateDownloads(mTime);
        int fIdx = getFileIdx(mFileID);
        fileSize[fIdx] = mSize;
        nodesWithFile[fIdx].add(mComA);
        hasFile[mComA][fIdx] = true;

        for (Download d : activeList) {
            if (!d.isFinished && d.fileIdx == fIdx) {
                if (dist[d.reqNode][mComA] <= 5000) d.activeSources++;
            }
        }
    }

    public int downloadFile(int mTime, int mComA, int mFileID) {
        updateDownloads(mTime);
        int fIdx = getFileIdx(mFileID);
         
        int count = 0;
        int[] rowA = dist[mComA];
        for (int sourceNode : nodesWithFile[fIdx]) {
            if (rowA[sourceNode] <= 5000) count++;
        }

        Download d = new Download();
        d.reqNode = mComA;
        d.fileIdx = fIdx;
        d.totalSize = fileSize[fIdx];
        d.downloaded = 0;
        d.lastTime = mTime;
        d.activeSources = count;
        d.isFinished = false;

        activeList.add(d);
        nodeDownloadMap[mComA][fIdx] = d;
        return count;
    }

    public int getFileSize(int mTime, int mComA, int mFileID) {
        updateDownloads(mTime);
        Integer fIdx = fileIdMap.get(mFileID);
        if (fIdx == null) return 0;

        if (hasFile[mComA][fIdx]) return fileSize[fIdx];

        Download d = nodeDownloadMap[mComA][fIdx];
        if (d != null) return (int) d.downloaded;

        return 0;
    }
}