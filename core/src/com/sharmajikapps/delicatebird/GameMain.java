package com.sharmajikapps.delicatebird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import scenes.MainMenu;

public class GameMain extends Game {
	private SpriteBatch batch;

	public AdsController adsController;

	public GameMain(AdsController adsController){
		this.adsController = adsController;
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		Music bgmusic = Gdx.audio.newMusic(Gdx.files.internal("Sounds/bgmusic.mp3"));
		bgmusic.setLooping(true);
		bgmusic.setVolume(0.1f);
		bgmusic.play();
		adsController.showBannerAd();

		setScreen(new MainMenu(this));

	}

	@Override
	public void render () {
		super.render();

	}

	public SpriteBatch getBatch() {
		return this.batch;
	}


}
