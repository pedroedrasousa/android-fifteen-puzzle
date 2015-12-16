package com.pedroedrasousa.engine;

import android.annotation.SuppressLint;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.pedroedrasousa.engine.EngineGlobal.DEBUG;

@SuppressLint("DefaultLocale")
public class Timer {
	
	private static final Logger logger = LoggerFactory.getLogger(Timer.class.getSimpleName());
	
	private static final int STATE_RUNNING	= 0;
	private static final int STATE_STOPPED	= 1;
	private static final int STATE_PAUSED	= 2;

	private long	startTime;
	private long	stopTime;
	private long	cumulativeTime;
	private int		state;
	
	public Timer() {
		reset();
	}
	
	public boolean isRunning() {
		return state == STATE_RUNNING;
	}
	
	public void reset() {
		
		if (DEBUG) logger.debug("Resetting timer. Previous state was {}.", getStatusString());
		
		startTime		= 0;
		stopTime		= 0;
		cumulativeTime	= 0;
		state			= STATE_STOPPED;
	}
	
	public void pause() {

		if (state != STATE_RUNNING) {
			logger.warn("Cannot pause timer. Current state is {}.", getStatusString());
			return;			
		}

		if (DEBUG) logger.debug("Pausing timer. Previous state was {}.", getStatusString());
		
		state = STATE_PAUSED;
		cumulativeTime += System.currentTimeMillis() - startTime;
	}
	
	public void resume() {
		resume(true);
	}
	
	public void resume(boolean forceStart) {
				
		if (state != STATE_PAUSED && !forceStart) {
			logger.warn("Cannot resume timer. Current state is {}.", getStatusString());
			return;
		}
		
		if (DEBUG) logger.debug("Resuming timer. Previous state was {}.", getStatusString());
		
		state = STATE_RUNNING;
		startTime = System.currentTimeMillis();
	}
	
	public void start() {
		if (DEBUG) logger.debug("Starting timer. Previous state was {}.", getStatusString());
		state = STATE_RUNNING;
		startTime = System.currentTimeMillis();
	}

	public void stop() {
		if (DEBUG) logger.debug("Stopping timer. Previous state was {}.", getStatusString());
		state = STATE_STOPPED;
		stopTime = System.currentTimeMillis();
	}
	
	public long getElapsedMs() {
		long ms;
		
		switch (state) {
		case STATE_RUNNING:
			ms = System.currentTimeMillis() - startTime + cumulativeTime;
			break;
		case STATE_PAUSED:
			ms = cumulativeTime;
			break;
		case STATE_STOPPED:
			ms = stopTime - startTime + cumulativeTime;
			break;
		default:
			ms = 0;
		}
		
		return ms;
	}
	
	public String getElapsedTimeString() {
		long ms = getElapsedMs();
		
		int seconds = (int) (ms / 1000);
		int minutes = seconds / 60;
		seconds     = seconds % 60;
		
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	public int getElapsedMinutes() {
		return (int)(getElapsedMs() / 1000 / 60);
	}
	
	public int getElapsedSeconds() {
		return (int)(getElapsedMs() / 1000);
	}
	
	public static String secToStringMMSS(int sec) {
		long minute = TimeUnit.SECONDS.toMinutes(sec);
		long second = TimeUnit.SECONDS.toSeconds(sec) - (TimeUnit.SECONDS.toMinutes(sec) * 60);
		return String.format("%02d:%02d", minute, second);
	}
	
	private String getStatusString() {
		
		switch (state) {
		case 0:
			return "Running";
			
		case 1:
			return "Stopped";
			
		case 2:
			return "Paused";
			
		default:
			return "Undetermined";
			
		}
	}
}
