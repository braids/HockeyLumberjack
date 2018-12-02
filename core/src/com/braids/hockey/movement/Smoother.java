package com.braids.hockey.movement;

import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class Smoother implements SmoothableGraphPath<Vector2, Vector2> {

    Array<Vector2> path = new Array<Vector2>();

    @Override
    public Vector2 getNodePosition(int index) {
        return path.get(index);
    }

    @Override
    public void swapNodes(int index1, int index2) {
        path.swap(index1, index2);
    }

    @Override
    public void truncatePath(int newLength) {
        path.truncate(newLength);
    }

    @Override
    public int getCount() {
        return path.size;
    }

    @Override
    public Vector2 get(int index) {
        return path.get(index);
    }

    @Override
    public void add(Vector2 node) {
        path.add(node);
    }

    @Override
    public void clear() {
        path.clear();
    }

    @Override
    public void reverse() {
        path.reverse();
    }

    @Override
    public Iterator<Vector2> iterator() {
        return path.iterator();
    }
}
