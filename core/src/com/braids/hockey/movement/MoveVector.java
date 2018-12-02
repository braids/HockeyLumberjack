package com.braids.hockey.movement;

import com.badlogic.gdx.math.Vector2;

public class MoveVector {
    public static Vector2
            Right = Vector2.X.cpy(),
            Up = Vector2.Y.cpy(),
            Left = Vector2.X.cpy().scl(-1),
            Down = Vector2.Y.cpy().scl(-1),
            None = Vector2.Zero.cpy();
}

