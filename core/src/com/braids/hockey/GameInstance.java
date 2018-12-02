package com.braids.hockey;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.braids.hockey.screen.Level1;

public class GameInstance extends Game {
	public PolygonSpriteBatch batch;
	Music bgmusic;
	
	@Override
	public void create () {
		batch = new PolygonSpriteBatch();

		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);

		bgmusic = Gdx.audio.newMusic(Gdx.files.internal("nazihunter_loop.wav"));
		bgmusic.setVolume(0.7f);
		bgmusic.setLooping(true);
		bgmusic.play();

		this.setScreen(new Level1(this));
	}

	@Override
	public void render () { super.render(); }
	
	@Override
	public void dispose () {
		bgmusic.dispose();
		batch.dispose();
	}
}
