package com.tuohy.worldwindvr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.tuohy.worldwindvr.input.VRFlyViewInputHandler;
import com.tuohy.worldwindvr.scratch.Throwaway3dModelsLayer;

import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.collada.impl.ColladaController;

public class GameLogic implements PositionListener, RenderingListener {

	private WorldWindVR worldWindVR;
	private Throwaway3dModelsLayer layer;
	List<ColladaController> powerups = new ArrayList<ColladaController>();
	
	public GameLogic(WorldWindVR worldWindVR) {
		// TODO Auto-generated constructor stub
		this.worldWindVR = worldWindVR;
		this.layer = new Throwaway3dModelsLayer(this.worldWindVR);
		//layer.createTrex(Position.fromDegrees(36.173121213137755,-111.69061780538789,863));
		layer.createYoshi(Position.fromDegrees(36.173121213137755,-111.69061780538789,963));
	}
	

	
	public void moved(PositionEvent event){
		//System.out.println("moved");
	}

	@Override
	public void stageChanged(RenderingEvent event) {
		//System.out.println("stageChanged");
		//System.out.println(this.worldWindVR.view.getEyePosition());
		// TODO Auto-generated method stub
		Random generator = new Random();
		if(generator.nextDouble() < 0.003) {
			Position position = Position.fromDegrees(this.worldWindVR.view.getEyePosition().getLatitude().getDegrees(), this.worldWindVR.view.getEyePosition().getLongitude().getDegrees() + .04, this.worldWindVR.view.getEyePosition().getElevation());
			ColladaController powerup = layer.createYoshi(position);
			this.powerups.add(powerup);
		}
		
		final WorldWindVR wwvr = this.worldWindVR;
		for(ColladaController powerup : powerups) {
			Vec4 current = this.worldWindVR.wwd.getModel().getGlobe().computePointFromPosition(this.worldWindVR.view.getEyePosition());
			Vec4 powerupPosition = this.worldWindVR.wwd.getModel().getGlobe().computePointFromPosition(powerup.getColladaRoot().getPosition());
			System.out.println(powerupPosition.distanceTo3(current));
			if(powerupPosition.distanceTo3(current) < 1050) {
				layer.removeCollada(powerup);
				((VRFlyViewInputHandler) wwvr.wwd.getView().getViewInputHandler()).setCameraTranslationSpeed(30.0);
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						((VRFlyViewInputHandler) wwvr.wwd.getView().getViewInputHandler()).setCameraTranslationSpeed(12.0);
					}
				}, 5*1000);
			}
		}
		
		

		
	}
	
	
}
