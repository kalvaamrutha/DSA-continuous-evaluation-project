/*
 * ============================================================
 *  Uber Bengaluru — Surge Multiplier on Geofenced Zone Array
 *  Segment Tree with Lazy Propagation (Range Add + Range Max)
 *  DSA-2 Case Study | FED Department
 * ============================================================
 *
 *  Operations supported:
 *    update(l, r, delta) — add delta to all zones in [l, r]
 *    query(l, r)         — return max multiplier in [l, r]
 *
 *  Complexity:
 *    Build  : O(n)
 *    Update : O(log n)
 *    Query  : O(log n)
 *    Space  : O(n)
 *
 *  Trace from 7:00 PM – 7:01 PM (n=16 zones, all start at 1.0):
 *    1. update [3,  9] += 0.5   (M.G. Road event ends)
 *    2. update [7, 14] += 0.3   (Whitefield IT shift ends)
 *    3. query  [0, 15]          → 1.8  (zones 7-9 got both deltas)
 *    4. update [2,  6] += 0.7   (Cricket stadium empties)
 *    5. query  [4, 10]          → 2.2  (zones 4-6: 1.0+0.5+0.7)
 * ============================================================
 */

public class UberSegTree {

    // ─────────────────────────────────────────────────────────
    //  Segment Tree (1-indexed, size = 4*n)
    // ─────────────────────────────────────────────────────────
    static double[] maxVal; // max surge in node's range
    static double[] lazy;   // pending add-delta not yet pushed down
    static int N;

    // Apply a delta to a single node (no recursion)
    static void apply(int node, double delta) {
        maxVal[node] += delta;
        lazy[node]   += delta;
    }

    // Push pending lazy from parent to both children
    static void pushDown(int node) {
        if (lazy[node] != 0.0) {
            apply(2 * node,     lazy[node]);
            apply(2 * node + 1, lazy[node]);
            lazy[node] = 0.0;
        }
    }

    // Pull max from children into parent
    static void pullUp(int node) {
        maxVal[node] = Math.max(maxVal[2 * node], maxVal[2 * node + 1]);
    }

    // ── Build ─────────────────────────────────────────────
    static void build(int node, int l, int r, double[] arr) {
        lazy[node] = 0.0;
        if (l == r) {
            maxVal[node] = arr[l];
            return;
        }
        int mid = (l + r) / 2;
        build(2 * node,     l,     mid, arr);
        build(2 * node + 1, mid + 1, r, arr);
        pullUp(node);
    }

    // ── Range Add (lazy propagation) ──────────────────────
    static void update(int node, int l, int r, int ql, int qr, double delta) {
        if (qr < l || r < ql) return;             // completely outside
        if (ql <= l && r <= qr) {                 // completely inside
            apply(node, delta);
            return;
        }
        pushDown(node);                           // partial overlap
        int mid = (l + r) / 2;
        update(2 * node,     l,     mid, ql, qr, delta);
        update(2 * node + 1, mid + 1, r, ql, qr, delta);
        pullUp(node);
    }

    // ── Range Max Query ───────────────────────────────────
    static double query(int node, int l, int r, int ql, int qr) {
        if (qr < l || r < ql) return Double.NEGATIVE_INFINITY;
        if (ql <= l && r <= qr) return maxVal[node];
        pushDown(node);
        int mid = (l + r) / 2;
        return Math.max(
            query(2 * node,     l,     mid, ql, qr),
            query(2 * node + 1, mid + 1, r, ql, qr)
        );
    }

    // ── Public wrappers ───────────────────────────────────
    static void update(int l, int r, double delta) {
        update(1, 0, N - 1, l, r, delta);
    }

    static double query(int l, int r) {
        return query(1, 0, N - 1, l, r);
    }

    // ── Print all leaf values ─────────────────────────────
    static void printLeaves() {
        System.out.print("  Zones: ");
        for (int i = 0; i < N; i++) {
            System.out.printf("%.1f", query(i, i));
            if (i < N - 1) System.out.print("  ");
        }
        System.out.println();
    }

    // ── Verify helper ─────────────────────────────────────
    static void check(String label, double got, double expected) {
        boolean ok = Math.abs(got - expected) < 1e-9;
        System.out.printf("  %-38s = %.1f  [%s]%n", label, got, ok ? "PASS" : "FAIL");
    }

    // ─────────────────────────────────────────────────────────
    //  Main
    // ─────────────────────────────────────────────────────────
    public static void main(String[] args) {

        N = 16;                          // 16 geofenced zones (0..15)
        maxVal = new double[4 * N];
        lazy   = new double[4 * N];

        double[] arr = new double[N];
        for (int i = 0; i < N; i++) arr[i] = 1.0;   // all zones start at 1.0

        build(1, 0, N - 1, arr);

        System.out.println("================================================");
        System.out.println("  Uber Bengaluru — Surge Multiplier Engine");
        System.out.println("  Segment Tree | Lazy Propagation | n = " + N);
        System.out.println("================================================\n");

        System.out.println("Initial state (all zones = 1.0):");
        printLeaves();
        System.out.println();

        // ── Step 1 ───────────────────────────────────────
        System.out.println("Step 1 | update [3, 9] += 0.5  (M.G. Road event ends)");
        update(3, 9, 0.5);
        printLeaves();
        System.out.println();

        // ── Step 2 ───────────────────────────────────────
        System.out.println("Step 2 | update [7, 14] += 0.3  (Whitefield IT shift ends)");
        update(7, 14, 0.3);
        printLeaves();
        System.out.println();

        // ── Step 3 ───────────────────────────────────────
        double ans3 = query(0, 15);
        System.out.printf("  Step 3 | query max [0, 15] = %.1f%n", ans3);
        System.out.println("  (Zones 7-9 got both +0.5 and +0.3 → 1.8)\n");

        // ── Step 4 ───────────────────────────────────────
        System.out.println("Step 4 | update [2, 6] += 0.7  (Cricket stadium empties)");
        update(2, 6, 0.7);
        printLeaves();
        System.out.println();

        // ── Step 5 ───────────────────────────────────────
        double ans5 = query(4, 10);
        System.out.printf("  Step 5 | query max [4, 10] = %.1f%n", ans5);
        System.out.println("  (Zones 4-6: 1.0+0.5+0.7 = 2.2; zones 7-9: 1.0+0.5+0.3 = 1.8)\n");

        // ── Verification ─────────────────────────────────
        System.out.println("================================================");
        System.out.println("  Verification");
        System.out.println("================================================");
        check("query max [0,15] after steps 1-2", ans3, 1.8);
        check("query max [4,10] after steps 1-4", ans5, 2.2);
        check("zone  0 (no update)",               query(0,  0),  1.0);
        check("zone  3 (+0.5 +0.7)",               query(3,  3),  2.2);
        check("zone  6 (+0.5 +0.7)",               query(6,  6),  2.2);
        check("zone  7 (+0.5 +0.3)",               query(7,  7),  1.8);
        check("zone 10 (+0.3 only)",               query(10, 10), 1.3);
        check("zone 15 (no update)",               query(15, 15), 1.0);

        System.out.println();
        System.out.println("================================================");
        System.out.println("  Complexity Summary");
        System.out.println("================================================");
        System.out.println("  Build           : O(n)       n = " + N);
        System.out.println("  Range Update    : O(log n)   log2(16) = 4 levels");
        System.out.println("  Range Max Query : O(log n)   log2(16) = 4 levels");
        System.out.println("  Space           : O(n)       " + (4 * N) + " nodes allocated");
        System.out.println("================================================");
    }
}