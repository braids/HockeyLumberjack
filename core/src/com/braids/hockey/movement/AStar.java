package com.braids.hockey.movement;

import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.IntArray;

public class AStar {
    private final int width, height;
    private final BinaryHeap<PathNode> open;
    private final PathNode[] nodes;
    int runID;
    private final IntArray path = new IntArray();
    private int targetX, targetY;
    private PathSmoother<PathNode, Vector2> psmoother;
    public Array<Vector2> smoothPath = new Array<Vector2>();
    int maxPaths = 6;

    public AStar(int width, int height) {
        this.width = width;
        this.height = height;
        open = new BinaryHeap(width * 4, false);
        nodes = new PathNode[width * height];
    }

    public IntArray getPath (int startX, int startY, int targetX, int targetY) {
        this.targetX = targetX; // Store target coordinates
        this.targetY = targetY;

        path.clear();   // Clear path and
        open.clear();

        runID++;
        if (runID < 0) runID = 1;

        int index = startY * width + startX;
        PathNode root = nodes[index];
        if (root == null) {
            root = new PathNode(0);
            root.x = startX;
            root.y = startY;
            nodes[index] = root;
        }
        root.parent = null;
        root.pathCost = 0;
        open.add(root, 0);

        int lastColumn = width - 1, lastRow = height - 1;
        int i = 0;
        while (open.size > 0) {
            PathNode node = open.pop();
            if(node.x == targetX && node.y == targetY) {
                while (node != root) {
                    path.add(node.x);
                    path.add(node.y);
                    node = node.parent;
                }
                break;
            }
            node.closedID = runID;

            // Add nodes around current position
            int x = node.x;
            int y = node.y;
            if (x < lastColumn) {
                addNode(node, x + 1, y, 10);
                if (y < lastRow) addNode(node, x + 1, y + 1, 14);
                if (y > 0) addNode(node, x + 1, y - 1, 14);
            }
            if (x > 0) {
                addNode(node, x - 1, y, 10);
                if (y < lastRow) addNode(node, x - 1, y + 1, 14);
                if (y > 0) addNode(node, x - 1, y - 1, 14);
            }
            if (y < lastRow) addNode(node, x, y + 1, 10);
            if (y > 0) addNode(node, x, y - 1, 10);
            i++;
        }

        smoothVectorPath();

        return path;
    }

    private void addNode (PathNode parent, int x, int y, int cost) {
        if (!isValid(x, y)) return;

        int pathCost = parent.pathCost + cost;
        float score = pathCost + Math.abs(x - targetX) + Math.abs(y - targetY);

        int index = y * width + x;
        PathNode node = nodes[index];
        if (node != null && node.runID == runID) {  // If node already encountered
            if (node.closedID != runID && pathCost < node.pathCost) {   // Node isn't closed and new cost is lower
                open.setValue(node, score);
                node.parent = parent;
                node.pathCost = pathCost;
            }
        }
        else { // Node not yet encountered
            if (node == null) {
                node = new PathNode(0);
                node.x = x;
                node.y = y;
                nodes[index] = node;
            }
            open.add(node, score);
            node.runID = runID;
            node.parent = parent;
            node.pathCost = pathCost;
        }
    }

    private void smoothVectorPath() {
        smoothPath.clear();

        if(path.size <= 1)
            return;

        for(int i = 0; i < path.size - 2; i += 2) {
            smoothPath.add(new Vector2(path.get(i),path.get(i + 1)));
        }

        Smoother smoothable = new Smoother();

        for(Vector2 point : smoothPath)
            smoothable.add(point);

        PathSmoother<Vector2,Vector2> pathSmoother = new PathSmoother<Vector2, Vector2>(new RaycastDetector());

        pathSmoother.smoothPath(smoothable);

        smoothPath.clear();

        for(Vector2 point : smoothable.path)
            smoothPath.add(point);

        if (smoothPath.size > maxPaths) {
            smoothPath.removeRange(0, smoothPath.size - maxPaths - 1);
        }
    }

    protected boolean isValid (int x, int y) {
        return true;
    }

    public int getWidth () {
        return width;
    }

    public int getHeight () {
        return height;
    }

}
