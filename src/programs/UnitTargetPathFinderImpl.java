package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int GRID_MAX_X = 27;
    private static final int GRID_MAX_Y = 21;

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        PathfindingContext context = new PathfindingContext(
            attackUnit.getxCoordinate(), attackUnit.getyCoordinate(),
            targetUnit.getxCoordinate(), targetUnit.getyCoordinate()
        );

        ObstacleMap obstacleMap = new ObstacleMap(existingUnitList, context);
        AStarSolver solver = new AStarSolver(context, obstacleMap);

        return solver.solve();
    }

    private static class PathfindingContext {
        final int startX, startY, goalX, goalY;

        PathfindingContext(int startX, int startY, int goalX, int goalY) {
            this.startX = startX;
            this.startY = startY;
            this.goalX = goalX;
            this.goalY = goalY;
        }

        boolean isGoal(int x, int y) {
            return x == goalX && y == goalY;
        }

        int heuristic(int x, int y) {
            return Math.abs(x - goalX) + Math.abs(y - goalY);
        }
    }

    private static class ObstacleMap {
        private final Set<Long> blocked;

        ObstacleMap(List<Unit> units, PathfindingContext ctx) {
            blocked = new HashSet<>();

            for (Unit u : units) {
                if (u != null && u.isAlive()) {
                    int ux = u.getxCoordinate();
                    int uy = u.getyCoordinate();

                    if (!(ux == ctx.startX && uy == ctx.startY) &&
                        !(ux == ctx.goalX && uy == ctx.goalY)) {
                        blocked.add(encode(ux, uy));
                    }
                }
            }
        }

        boolean isBlocked(int x, int y) {
            return blocked.contains(encode(x, y));
        }

        private long encode(int x, int y) {
            return ((long)x << 32) | (y & 0xFFFFFFFFL);
        }
    }

    private static class AStarSolver {
        private final PathfindingContext ctx;
        private final ObstacleMap obstacles;
        private final Map<Long, SearchNode> visited;
        private final PriorityQueue<SearchNode> frontier;

        AStarSolver(PathfindingContext ctx, ObstacleMap obstacles) {
            this.ctx = ctx;
            this.obstacles = obstacles;
            this.visited = new HashMap<>();
            this.frontier = new PriorityQueue<>(new Comparator<SearchNode>() {
                public int compare(SearchNode a, SearchNode b) {
                    return Integer.compare(a.fScore, b.fScore);
                }
            });
        }

        List<Edge> solve() {
            SearchNode start = new SearchNode(ctx.startX, ctx.startY, 0, ctx.heuristic(ctx.startX, ctx.startY), null);
            frontier.offer(start);
            visited.put(encodePos(ctx.startX, ctx.startY), start);

            while (!frontier.isEmpty()) {
                SearchNode current = frontier.poll();

                if (ctx.isGoal(current.x, current.y)) {
                    return reconstructPath(current);
                }

                expandNode(current);
            }

            return new ArrayList<>();
        }

        private void expandNode(SearchNode node) {
            int[][] directions = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};

            for (int[] dir : directions) {
                int nx = node.x + dir[0];
                int ny = node.y + dir[1];

                if (!isValid(nx, ny)) continue;
                if (obstacles.isBlocked(nx, ny) && !ctx.isGoal(nx, ny)) continue;

                int newG = node.gScore + 1;
                long posKey = encodePos(nx, ny);
                SearchNode existing = visited.get(posKey);

                if (existing == null || newG < existing.gScore) {
                    SearchNode neighbor = new SearchNode(nx, ny, newG, newG + ctx.heuristic(nx, ny), node);
                    visited.put(posKey, neighbor);
                    frontier.offer(neighbor);
                }
            }
        }

        private boolean isValid(int x, int y) {
            return x >= 0 && x < GRID_MAX_X && y >= 0 && y < GRID_MAX_Y;
        }

        private List<Edge> reconstructPath(SearchNode goal) {
            List<Edge> path = new ArrayList<>();
            SearchNode current = goal;

            while (current != null) {
                path.add(new Edge(current.x, current.y));
                current = current.parent;
            }

            int left = 0, right = path.size() - 1;
            while (left < right) {
                Edge temp = path.get(left);
                path.set(left, path.get(right));
                path.set(right, temp);
                left++;
                right--;
            }

            return path;
        }

        private long encodePos(int x, int y) {
            return ((long)x << 32) | (y & 0xFFFFFFFFL);
        }
    }

    private static class SearchNode {
        final int x, y, gScore, fScore;
        final SearchNode parent;

        SearchNode(int x, int y, int g, int f, SearchNode parent) {
            this.x = x;
            this.y = y;
            this.gScore = g;
            this.fScore = f;
            this.parent = parent;
        }
    }
}
