// MediFlowAVL.java — complete single-file AVL Tree implementation
// MediFlow Hospital: appointment-driven patient indexing
// Compile: javac MediFlowAVL.java
// Run    : java MediFlowAVL

/* ══════════════════════════════════════════════════════════
   1. AVLNode — patient record node
   ══════════════════════════════════════════════════════════ */
class AVLNode {
    int patientId;
    int height;
    AVLNode left, right;

    AVLNode(int id) {
        this.patientId = id;
        this.height    = 1;
        left = right   = null;
    }
}

/* ══════════════════════════════════════════════════════════
   2. AVLTree — self-balancing BST
   ══════════════════════════════════════════════════════════ */
class AVLTree {

    AVLNode root;

    // ── Height helpers ───────────────────────────────────────
    private int height(AVLNode n) {
        return n == null ? 0 : n.height;
    }

    private int bf(AVLNode n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    private void updateH(AVLNode n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    // ── Rotations ────────────────────────────────────────────
    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left, T2 = x.right;
        x.right = y; y.left = T2;
        updateH(y); updateH(x);
        System.out.println("    [LL] Right rotation on " + y.patientId);
        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right, T2 = y.left;
        y.left = x; x.right = T2;
        updateH(x); updateH(y);
        System.out.println("    [RR] Left rotation on " + x.patientId);
        return y;
    }

    // ── Balance ──────────────────────────────────────────────
    private AVLNode balance(AVLNode n, int key) {
        updateH(n);
        int b = bf(n);

        if (b > 1  && key < n.left.patientId)  return rotateRight(n);           // LL
        if (b < -1 && key > n.right.patientId) return rotateLeft(n);            // RR
        if (b > 1  && key > n.left.patientId) {                                    // LR
            System.out.println("    [LR] Left-Right rotation on " + n.patientId);
            n.left = rotateLeft(n.left); return rotateRight(n);
        }
        if (b < -1 && key < n.right.patientId) {                                   // RL
            System.out.println("    [RL] Right-Left rotation on " + n.patientId);
            n.right = rotateRight(n.right); return rotateLeft(n);
        }
        return n;
    }

    // ── Insert ───────────────────────────────────────────────
    void insert(int id) {
        System.out.printf("Insert %d:%n", id);
        root = insertRec(root, id);
        System.out.printf("  height=%d  rootBF=%d%n%n", height(root), bf(root));
    }

    private AVLNode insertRec(AVLNode n, int key) {
        if (n == null) return new AVLNode(key);
        if      (key < n.patientId) n.left  = insertRec(n.left,  key);
        else if (key > n.patientId) n.right = insertRec(n.right, key);
        else return n;
        return balance(n, key);
    }

    // ── Delete ───────────────────────────────────────────────
    void delete(int id, String reason) {
        System.out.printf("Delete %d (%s):%n", id, reason);
        root = deleteRec(root, id);
        System.out.printf("  height=%d%n%n", height(root));
    }

    private AVLNode deleteRec(AVLNode n, int key) {
        if (n == null) return null;
        if      (key < n.patientId) n.left  = deleteRec(n.left,  key);
        else if (key > n.patientId) n.right = deleteRec(n.right, key);
        else {
            if (n.left  == null) return n.right;
            if (n.right == null) return n.left;
            AVLNode s = n.right;
            while (s.left != null) s = s.left;   // in-order successor
            n.patientId = s.patientId;
            n.right = deleteRec(n.right, s.patientId);
        }
        return balance(n, n.patientId);
    }

    // ── Search (triage point-lookup) ─────────────────────────
    boolean search(int id) {
        int depth = searchRec(root, id, 0);
        if (depth >= 0) {
            System.out.printf("  Found %d at depth %d  (~%d ns) — SLA %s%n",
                id, depth, depth * 200L,
                (depth * 200L < 5_000_000) ? "OK ✓" : "BREACH ✗");
            return true;
        }
        System.out.printf("  Patient %d NOT found%n", id);
        return false;
    }

    private int searchRec(AVLNode n, int key, int d) {
        if (n == null)           return -1;
        if (key == n.patientId)   return d;
        return key < n.patientId ? searchRec(n.left, key, d+1) : searchRec(n.right, key, d+1);
    }

    // ── Traversals ───────────────────────────────────────────
    void inorder() {
        System.out.print("In-order: ");
        inRec(root);
        System.out.println();
    }

    private void inRec(AVLNode n) {
        if (n == null) return;
        inRec(n.left);
        System.out.print(n.patientId + " ");
        inRec(n.right);
    }

    void topKDescending(int k) {
        System.out.printf("Top %d descending: ", k);
        int[] c = {0};
        revRec(root, k, c);
        System.out.println();
    }

    private void revRec(AVLNode n, int k, int[] c) {
        if (n == null || c[0] >= k) return;
        revRec(n.right, k, c);
        if (c[0] < k) { System.out.print(n.patientId + " "); c[0]++; }
        revRec(n.left, k, c);
    }

    void slaReport() {
        int h = height(root);
        long ns = h * 200L;
        System.out.println("\n══ SLA Budget Report ══════════════════════════");
        System.out.printf("  Tree height         : %d (ceiling: 25)%n", h);
        System.out.printf("  p99 latency est.    : ~%,d ns%n", ns);
        System.out.printf("  SLA (< 5,000,000 ns): %s%n", ns < 5_000_000 ? "PASS ✓" : "FAIL ✗");
        System.out.println("══════════════════════════════════════════════");
    }
}

/* ══════════════════════════════════════════════════════════
   3. MediFlowAVL — main driver
   ══════════════════════════════════════════════════════════ */
public class MediFlowAVL {

    public static void main(String[] args) {

        AVLTree avl = new AVLTree();

        /* ── Phase 1: Morning insertions ──────────────────────── */
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Phase 1 — Morning Appointment Insertions    ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        int[] ids = {20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90};
        for (int id : ids) avl.insert(id);

        avl.inorder();
        System.out.println();

        /* ── Phase 2: Triage point-lookups ────────────────────── */
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Phase 2 — Triage Point-Lookups              ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        avl.search(65);   // exists
        avl.search(45);   // exists
        avl.search(99);   // not found
        avl.slaReport();

        /* ── Phase 3: Noon deletions ──────────────────────────── */
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║  Phase 3 — Noon Deletions                    ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        avl.delete(30, "transferred");
        avl.delete(70, "discharged");
        avl.delete(50, "admission closed");

        avl.inorder();
        System.out.println();

        /* ── Phase 4: Top-K query ─────────────────────────────── */
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Phase 4 — Top-K Descending Query            ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        avl.topKDescending(5);
        avl.slaReport();
    }
}