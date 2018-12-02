package com.braids.hockey.movement;

import com.badlogic.gdx.utils.BinaryHeap;

class PathNode extends BinaryHeap.Node {
    int runID, closedID, x, y, pathCost;
    PathNode parent;

    public PathNode(float value) {
        super(value);
    }
}
