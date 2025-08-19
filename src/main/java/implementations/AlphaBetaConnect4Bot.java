package implementations;

import interfaces.Connect4Bot;

/**
 * Advanced Connect4 bot using Alpha-Beta pruning algorithm for optimal play.
 * Implements minimax with alpha-beta pruning to efficiently search game tree.
 * Code is intentionally compact to minimize token count.
 */
public class AlphaBetaConnect4Bot implements Connect4Bot {
    // Standard Connect4 dimensions
    private static final int ROWS = 6, COLS = 7;
    // Move ordering: center-first strategy for better alpha-beta pruning
    private static final int[] ORDER = {3,2,4,1,5,0,6};

    @Override
    public int play(int[][] b, int me) {
        // Determine opponent player number
        int opp = me == 1 ? 2 : 1;
        // Immediate win/block check: try each column in optimal order
        for (int c : ORDER) if (b[0][c]==0){int r=drop(b,c,me);boolean w=isWin(b,r,c,me);b[r][c]=0;if(w)return c; r=drop(b,c,opp);w=isWin(b,r,c,opp);b[r][c]=0;if(w)return c;}
        // Alpha-beta search parameters: depth=7, initial alpha/beta bounds
        int depth = 7, best = -1, bestScore = Integer.MIN_VALUE, a = -1_000_000_000, z = 1_000_000_000;
        // Search all valid moves using negamax with alpha-beta pruning
        for (int c : ORDER) {
            if (b[0][c]!=0) continue;
            int r = drop(b,c,me);
            int s = -negamax(b, depth-1, -z, -a, opp, me);
            b[r][c]=0;
            if (s > bestScore) { bestScore = s; best = c; }
            if (bestScore > a) a = bestScore;
        }
        // Fallback: play any valid move if no best move found
        if (best == -1) { for (int c = 0; c < COLS; c++) if (b[0][c]==0) return c; return 0; }
        return best;
    }

    /**
     * Negamax algorithm with alpha-beta pruning.
     * @param b board state
     * @param d remaining depth
     * @param a alpha (best score for maximizing player)
     * @param z beta (best score for minimizing player)
     * @param player current player
     * @param me the bot's player number
     * @return evaluated score from current player's perspective
     */
    private int negamax(int[][] b, int d, int a, int z, int player, int me) {
        // Base case: evaluate position at leaf nodes
        if (d == 0) return (player==me ? 1 : -1) * eval(b, me);
        int best = Integer.MIN_VALUE; boolean any=false; int next = player==1?2:1;
        // Try all valid moves in optimal order
        for (int c : ORDER) {
            if (b[0][c]!=0) continue; any=true;
            int r = drop(b,c,player);
            // Recursive negamax call with negated alpha/beta bounds
            int s = -negamax(b, d-1, -z, -a, next, me);
            b[r][c]=0;
            if (s > best) best = s;
            // Alpha-beta pruning: update alpha and check for cutoff
            if (best > a) a = best;
            if (a >= z) return a; // Beta cutoff
        }
        // Return 0 for draw positions, best score otherwise
        return any ? best : 0;
    }

    /**
     * Board evaluation function - counts potential 4-in-a-rows.
     * @param b board state
     * @param me the bot's player number
     * @return evaluation score (positive favors bot, negative favors opponent)
     */
    private int eval(int[][] b, int me) {
        int opp = me == 1 ? 2 : 1, s = 0; int[] dr={0,1,1,1}, dc={1,0,1,-1};
        // Check all positions and directions (horizontal, vertical, both diagonals)
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) for (int i=0;i<4;i++) {
            int r3=r+dr[i]*3, c3=c+dc[i]*3; if (r3<0||r3>=ROWS||c3<0||c3>=COLS) continue;
            // Count pieces in 4-cell window
            int m=0,o=0; for (int k=0;k<4;k++){int v=b[r+dr[i]*k][c+dc[i]*k]; if (v==me) m++; else if (v==opp) o++;}
            // Score based on potential: 4-in-row=100000, 3-in-row=100
            if (o==0) s += m==4?100000:(m==3?100:0); else if (m==0) s -= o==4?100000:(o==3?100:0);
        }
        return s;
    }

    // Drop piece in column, return row where it landed
    private int drop(int[][] b, int c, int p) { for (int r=ROWS-1;r>=0;r--) if (b[r][c]==0) { b[r][c]=p; return r; } throw new IllegalStateException(); }

    /**
     * Check if placing a piece at (r,c) creates a winning 4-in-a-row.
     * @param b board state
     * @param r row position
     * @param c column position
     * @param p player number
     * @return true if this move wins the game
     */
    private boolean isWin(int[][] b, int r, int c, int p) {
        int[] dr={0,1,1,1}, dc={1,0,1,-1};
        // Check all 4 directions from placed piece
        for(int i=0;i<4;i++){int ct=1;int R=r+dr[i],C=c+dc[i];while(R>=0&&R<ROWS&&C>=0&&C<COLS&&b[R][C]==p){ct++;R+=dr[i];C+=dc[i];}R=r-dr[i];C=c-dc[i];while(R>=0&&R<ROWS&&C>=0&&C<COLS&&b[R][C]==p){ct++;R-=dr[i];C-=dc[i];}if(ct>=4)return true;}
        return false;
    }

    @Override
    public String getBotName() { return "AlphaBetaConnect4Bot"; }
}
