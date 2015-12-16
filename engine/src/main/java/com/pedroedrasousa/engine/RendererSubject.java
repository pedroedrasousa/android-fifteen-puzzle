package com.pedroedrasousa.engine;

public interface RendererSubject {
	void registerObserver(RendererObserver observer);
	void unRegisterObserver(RendererObserver observer);
	String getSurfaceID();
}
