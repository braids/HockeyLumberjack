package com.braids.hockey.collision;

import com.badlogic.gdx.math.Vector2;

public class LineSegment {
    Vector2 begin = new Vector2(), end = new Vector2();

    public LineSegment() {}

    public LineSegment(Vector2 begin, Vector2 end) {
        this.begin = begin;
        this.end = end;
    }

    public boolean equals(LineSegment segment) {
        return begin == segment.begin && end == segment.end;
    }

    public boolean equals(Vector2 begin, Vector2 end) {
        return equals(new LineSegment(begin, end));
    }

    public void set(LineSegment segment) {
        this.begin = segment.begin;
        this.end = segment.end;
    }

    public void set(Vector2 begin, Vector2 end) {
        set(new LineSegment(begin, end));
    }

    public Vector2 distance() {
        return end.cpy().sub(begin);
    }

    public float len() {
        return distance().len();
    }
}
