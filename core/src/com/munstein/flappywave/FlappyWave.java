package com.munstein.flappywave;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class FlappyWave extends ApplicationAdapter {

	SpriteBatch batch;
	Texture background;
	Texture gameOverBackground;
	Texture gameover;
	Texture topTube;
	Texture bottomTube;
	Texture[] birds;

	int flapState = 0;
	int numberOfTubes = 4;
	int score;
	int scoringTube = 0;
	int gameState = 0; //0 = Game is waiting player interaction / 1 = Game is running / 2 = Game over screen
	final int fontScale = 8;

	long endtime = 0;

	float gravity = 2;
	float recordedHeight;
	float gap = 400;
	float maxTubeOffset;
	float birdY = 0;
	float velocity = 0;
	float tubeVelocity = 4;
	float distanceBetweenTubes;
	float endgameTextWidth, gameOverTextWidth, startGameTextWidth, highScoreTextWidth;
	float[] tubeX = new float[numberOfTubes];
	float[] tubeOffset = new float[numberOfTubes];

	Random randomGenerator;

	Circle birdCircle;

	Rectangle[] topTubeRectangles;
	Rectangle[] bottomTubeRectangles;

	BitmapFont scoreMessage;
	BitmapFont highScoreMessage;
	BitmapFont endGameMessage;
	BitmapFont startGameMessage;
	BitmapFont gameOverMessage;

	Music sound;

	Preferences preferences;
	final String PREFERENCES_NAME = "preferences";
	final String PREFERENCES_HIGHSCORE = "highscore";

	final String END_GAME_MESSAGE = "R E S T A R T";
	final String START_GAME_MESSAGE = "F I G H T !";
	final String GAME_OVER_MESSAGE = "G A M E O V E R : (";
	final String HIGH_SCORE_MESSAGE = "R E C O R D : ";

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg5.jpg");
		gameOverBackground = new Texture("gameover.png");

		birdCircle = new Circle();
		scoreMessage = new BitmapFont();
		scoreMessage.setColor(Color.WHITE);
		scoreMessage.getData().setScale(fontScale);

		endGameMessage = new BitmapFont();
		endGameMessage.setColor(Color.PINK);
		endGameMessage.getData().setScale(fontScale);

		startGameMessage = new BitmapFont();
		startGameMessage.setColor(Color.PURPLE);
		startGameMessage.getData().setScale(fontScale);

		gameOverMessage = new BitmapFont();
		gameOverMessage.setColor(Color.WHITE);
		gameOverMessage.getData().setScale(fontScale);

		highScoreMessage = new BitmapFont();
		highScoreMessage.setColor(Color.CYAN);
		highScoreMessage.getData().setScale(fontScale);

		preferences = Gdx.app.getPreferences("preferences");
		birds = new Texture[3];

		birds[0] = new Texture("playerup.png"); //face is up
		birds[1] = new Texture("playerfall.png"); // face is down

		topTube = new Texture("can.png");
		bottomTube = new Texture("can.png");
		maxTubeOffset = Gdx.graphics.getHeight() / 2 - gap / 2 - 100;
		randomGenerator = new Random();
		distanceBetweenTubes = Gdx.graphics.getWidth() * 3 / 4;
		topTubeRectangles = new Rectangle[numberOfTubes];
		bottomTubeRectangles = new Rectangle[numberOfTubes];

		Gdx.app.log("Score", preferences.getInteger(PREFERENCES_HIGHSCORE)+"");

		GlyphLayout layout = new GlyphLayout(endGameMessage, END_GAME_MESSAGE);
		endgameTextWidth = layout.width;

		layout = new GlyphLayout(endGameMessage, START_GAME_MESSAGE);
		startGameTextWidth = layout.width;

		layout = new GlyphLayout(highScoreMessage, HIGH_SCORE_MESSAGE + preferences.getInteger(PREFERENCES_HIGHSCORE));
		highScoreTextWidth = layout.width;

		startGame();
		startMusic();
	}

	public void startGame() {
		birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;
		endtime = 0;
		for (int i = 0; i < numberOfTubes; i++) {
			tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
			tubeX[i] = Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes;
			topTubeRectangles[i] = new Rectangle();
			bottomTubeRectangles[i] = new Rectangle();
		}
	}

	@Override
	public void render() {
		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (gameState == 1) {
			batch.draw(birds[flapState], Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2, birdY);
			if (tubeX[scoringTube] < Gdx.graphics.getWidth() / 2) {
				score++;
				if (scoringTube < numberOfTubes - 1) {
					scoringTube++;
				} else {
					scoringTube = 0;
				}
			}
			if (Gdx.input.justTouched()) {
				velocity = -20;
				flapState = 0;
				recordedHeight = birdY;
			}else{
				if(recordedHeight > (birdY +30) ){
					flapState = 1;
				}
			}

			for (int i = 0; i < numberOfTubes; i++) {
				if (tubeX[i] < - topTube.getWidth()) {
					tubeX[i] += numberOfTubes * distanceBetweenTubes;
					tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
				} else {
					tubeX[i] = tubeX[i] - tubeVelocity;
				}
				batch.draw(topTube, tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i]);
				batch.draw(bottomTube, tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight() + tubeOffset[i]);
				topTubeRectangles[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i], topTube.getWidth(), topTube.getHeight());
				bottomTubeRectangles[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight() + tubeOffset[i], bottomTube.getWidth(), bottomTube.getHeight());
			}

			if (birdY > 0) {
				velocity = velocity + gravity;
				birdY -= velocity;

			} else {
				gameState = 2;
			}

		} else if (gameState == 0) {
			if (Gdx.input.justTouched()) {
				gameState = 1;
				startGameMessage.dispose();
			}else{
				startGameMessage.draw(batch, START_GAME_MESSAGE,Gdx.graphics.getWidth() /2 - startGameTextWidth /2,
						Gdx.graphics.getHeight()/2);
			}

		} else if (gameState == 2) {
			gameOver();
		}

		scoreMessage.draw(batch, String.valueOf(score), 100, 200);

		birdCircle.set(Gdx.graphics.getWidth() / 2, birdY + birds[flapState].getHeight() / 2, birds[flapState].getWidth() / 2);

		for (int i = 0; i < numberOfTubes; i++) {
			if (Intersector.overlaps(birdCircle, topTubeRectangles[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {
				gameState = 2;
			}
		}

		batch.end();

	}

	public void saveHighScore(int currentScore){
		int maxScore = preferences.getInteger(PREFERENCES_HIGHSCORE, 0);
		if(currentScore > maxScore){
			preferences.putInteger(PREFERENCES_HIGHSCORE, currentScore);
			preferences.flush();
		}
	}


	public void startMusic(){
		sound = Gdx.audio.newMusic(Gdx.files.internal("song.mp3"));
		sound.play();
		sound.setLooping(true);
	}

	public void gameOver(){
		batch.draw(gameOverBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		saveHighScore(score);

		endGameMessage.draw(batch, "R E S T A R T",Gdx.graphics.getWidth() /2 - endgameTextWidth /2,
				Gdx.graphics.getHeight()/2);

		highScoreMessage.draw(batch, HIGH_SCORE_MESSAGE + preferences.getInteger(PREFERENCES_HIGHSCORE),Gdx.graphics.getWidth() /2 - highScoreTextWidth /2,
				Gdx.graphics.getHeight()/2 + 300);

		endtime = endtime == 0 ? TimeUtils.millis() : endtime;

		if(TimeUtils.timeSinceMillis(endtime)>900)
			if (Gdx.input.justTouched()) {
				gameState = 1;
				startGame();
				score = 0;
				scoringTube = 0;
				velocity = 0;
			}
	}

}