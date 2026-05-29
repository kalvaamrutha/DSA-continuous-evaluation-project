/*
 * ============================================================
 *  X (Twitter) — Retweet-Reach Prediction via Bounded BFS
 *  DSA-2 Case Study | FED Department
 * ============================================================
 *
 *  Problem:
 *    Given a follower graph, find the 10-minute retweet reach
 *    of a tweet from source user A, traversing outward via
 *    "is followed by" edges up to depth 3.
 *
 *    Naive BFS without a visited-set double-counts users
 *    reachable via multiple paths (E via B and C; G via D and E;
 *    H via E and F). Correct BFS uses a visited-set.
 *
 *  Graph (9 users, edges X→Y mean "X is followed by Y"):
 *    A → B, C
 *    B → D, E
 *    C → E, F
 *    D → G
 *    E → G, H
 *    F → H, I
 *
 *  Expected BFS from A (depth ≤ 3):
 *    depth 0 : A
 *    depth 1 : B, C
 *    depth 2 : D, E, F          (E counted once despite B→E and C→E)
 *    depth 3 : G, H, I          (G once; H once)
 *    Unique reach (excl. A) = 8
 *
 *  Complexity:
 *    Time  : O(V + E)  within depth limit
 *    Space : O(V)      visited-set + queue
 * ============================================================
 */

import java.util.*;

public class TwitterBFS {

    // ─────────────────────────────────────────────────────────
    //  Graph — directed adjacency list
    // ─────────────────────────────────────────────────────────
    static Map<String, List<String>> graph = new LinkedHashMap<>();

    static void addEdge(String from, String to) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        graph.computeIfAbsent(to,   k -> new ArrayList<>()); // ensure node exists
    }

    // ─────────────────────────────────────────────────────────
    //  BFS state: pair of (node, depth)
    // ─────────────────────────────────────────────────────────
    static class State {
        String node;
        int depth;
        State(String node, int depth) {
            this.node  = node;
            this.depth = depth;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Bounded BFS — returns unique reachable users (excl. source)
    // ─────────────────────────────────────────────────────────
    static Map<Integer, List<String>> bfs(String source, int maxDepth) {
        Map<Integer, List<String>> levelMap = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        Queue<State> queue  = new LinkedList<>();

        visited.add(source);
        queue.add(new State(source, 0));
        levelMap.put(0, Collections.singletonList(source));

        System.out.println("\n  [BFS TRACE]");
        System.out.printf("  Enqueue  %-4s  depth=0  (source)%n", source);

        while (!queue.isEmpty()) {
            State curr = queue.poll();

            if (curr.depth >= maxDepth) continue;  // depth limit reached

            List<String> neighbours = graph.getOrDefault(curr.node, Collections.emptyList());

            for (String nb : neighbours) {
                if (visited.contains(nb)) {
                    System.out.printf("  Skip     %-4s  (already visited — avoids double-count)%n", nb);
                    continue;
                }
                visited.add(nb);
                int d = curr.depth + 1;
                queue.add(new State(nb, d));
                levelMap.computeIfAbsent(d, k -> new ArrayList<>()).add(nb);
                System.out.printf("  Enqueue  %-4s  depth=%d  (via %s)%n", nb, d, curr.node);
            }
        }
        return levelMap;
    }

    // ─────────────────────────────────────────────────────────
    //  Naive BFS (no visited-set) — shows double-count problem
    // ─────────────────────────────────────────────────────────
    static int naiveBfs(String source, int maxDepth) {
        Queue<State> queue = new LinkedList<>();
        queue.add(new State(source, 0));
        int count = 0; // counts source too but we subtract later

        while (!queue.isEmpty()) {
            State curr = queue.poll();
            count++;
            if (curr.depth >= maxDepth) continue;
            for (String nb : graph.getOrDefault(curr.node, Collections.emptyList())) {
                queue.add(new State(nb, curr.depth + 1));
            }
        }
        return count - 1; // exclude source
    }

    // ─────────────────────────────────────────────────────────
    //  Main
    // ─────────────────────────────────────────────────────────
    public static void main(String[] args) {

        // ── Build follower graph ─────────────────────────
        addEdge("A", "B"); addEdge("A", "C");
        addEdge("B", "D"); addEdge("B", "E");
        addEdge("C", "E"); addEdge("C", "F");
        addEdge("D", "G");
        addEdge("E", "G"); addEdge("E", "H");
        addEdge("F", "H"); addEdge("F", "I");

        final String SOURCE    = "A";
        final int    MAX_DEPTH = 3;

        System.out.println("================================================");
        System.out.println("  X (Twitter) — Retweet-Reach Prediction");
        System.out.println("  Bounded BFS | source = " + SOURCE + " | max depth = " + MAX_DEPTH);
        System.out.println("================================================");

        System.out.println("\nFollower graph edges (X → Y : tweet by X visible to Y):");
        for (Map.Entry<String, List<String>> e : graph.entrySet()) {
            if (!e.getValue().isEmpty())
                System.out.println("  " + e.getKey() + "  →  " + e.getValue());
        }

        // ── Correct BFS with visited-set ─────────────────
        System.out.println("\n------------------------------------------------");
        System.out.println("  Correct BFS (with visited-set)");
        System.out.println("------------------------------------------------");

        Map<Integer, List<String>> levelMap = bfs(SOURCE, MAX_DEPTH);

        System.out.println("\n  Results by depth:");
        int uniqueReach = 0;
        for (Map.Entry<Integer, List<String>> e : levelMap.entrySet()) {
            System.out.printf("    depth %d : %s%n", e.getKey(), e.getValue());
            if (e.getKey() > 0) uniqueReach += e.getValue().size();
        }
        System.out.println("\n  Unique reach (excl. source A) = " + uniqueReach + " users");

        // ── Naive BFS without visited-set ────────────────
        System.out.println("\n------------------------------------------------");
        System.out.println("  Naive BFS (NO visited-set) — double-count demo");
        System.out.println("------------------------------------------------");
        int naiveCount = naiveBfs(SOURCE, MAX_DEPTH);
        System.out.println("  Naive count = " + naiveCount + " users  ← overcounts due to overlapping paths");
        System.out.println("  Overcounted by: " + (naiveCount - uniqueReach) + " duplicate visits");
        System.out.println("  Overlaps: E via B and C | G via D and E | H via E and F");

        // ── Verification ─────────────────────────────────
        System.out.println("\n------------------------------------------------");
        System.out.println("  Verification");
        System.out.println("------------------------------------------------");
        check("depth-1 users  = [B, C]",       levelMap.get(1).toString(), "[B, C]");
        check("depth-2 users  = [D, E, F]",     levelMap.get(2).toString(), "[D, E, F]");
        check("depth-3 users  = [G, H, I]",     levelMap.get(3).toString(), "[G, H, I]");
        check("unique reach   = 8",              String.valueOf(uniqueReach), "8");
        check("naive overcounts > 0",            String.valueOf(naiveCount > uniqueReach), "true");

        // ── Complexity summary ────────────────────────────
        System.out.println("\n================================================");
        System.out.println("  Complexity Summary");
        System.out.println("================================================");
        System.out.println("  Time  : O(V + E)  V=9 nodes, E=11 edges");
        System.out.println("  Space : O(V)      visited-set + BFS queue");
        System.out.println("  Depth : bounded at " + MAX_DEPTH + " hops (empirical retweet cutoff)");
        System.out.println("  SLA   : 500 ms per tweet at p99 — O(V+E) satisfies this");
        System.out.println("================================================");
    }

    static void check(String label, String got, String expected) {
        boolean ok = got.equals(expected);
        System.out.printf("  %-38s [%s]%n", label, ok ? "PASS" : "FAIL (got " + got + ")");
    }
}