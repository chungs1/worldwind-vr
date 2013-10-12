package com.tuohy.worldwindvr.scratch;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.collada.impl.ColladaController;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import com.tuohy.worldwindvr.WorldWindVR;

public class Throwaway3dModelsLayer {

	WorldWindVR vrFrame;
	private RenderableLayer layer;

	public Throwaway3dModelsLayer(WorldWindVR vrFrame){
		this.vrFrame = vrFrame;
        this.layer = new RenderableLayer();
        vrFrame.getWwd().getModel().getLayers().add(layer);
//		new WorkerThread(new File("testmodels/dinosaur.DAE"), Position.fromDegrees(36.19529915228048,-111.7481440380943,0), this).start();
		//new WorkerThread(new File("testmodels/trex/models/t-rex11.dae"), Position.fromDegrees(36.173121213137755,-111.69061780538789,863), this).start();
 		//new WorkerThread(new File("testmodels/trex/models/Yoshi (Complete).dae"), Position.fromDegrees(36.173121213137755,-111.99061780538789,963), this).start();
	}
		
	public ColladaController createTrex(Position position){
		return addLayer2(new File("testmodels/trex/models/t-rex11.dae"), position, new Vec4(30.0, 30.0, 30.0));
	}
	
	public ColladaController createYoshi(Position position){
		//WorkerThread WT = new WorkerThread(new File("testmodels/trex/models/Yoshi (Complete).dae"), position, new Vec4(900.0, 900.0, 900.0), this);
		//WT.start();
		return addLayer2(new File("testmodels/trex/models/Yoshi (Complete).dae"), position, new Vec4(900.0, 900.0, 900.0));
	}
	
	public void removeCollada(ColladaController root) {
		this.layer.removeRenderable(root);
	}
	
	public ColladaController addLayer2(Object colladaSource, Position position, Vec4 scale){
        ColladaRoot colladaRoot = null;
		try {
			colladaRoot = ColladaRoot.createAndParse(colladaSource);
	        colladaRoot.setPosition(position);
	        colladaRoot.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
	        colladaRoot.setModelScale(scale);
	        return addColladaLayer(colladaRoot);
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
     * Adds the specified <code>colladaRoot</code> to this app frame's <code>WorldWindow</code> as a new
     * <code>Layer</code>.
     *
     * @param colladaRoot the ColladaRoot to add a new layer for.
     */
    protected ColladaController addColladaLayer(ColladaRoot colladaRoot)
    {
        // Create a ColladaController to adapt the ColladaRoot to the World Wind renderable interface.
        ColladaController colladaController = new ColladaController(colladaRoot);

        // Adds a new layer containing the ColladaRoot to the end of the WorldWindow's layer list.
        layer.addRenderable(colladaController);
        
        System.out.println("added dinosaur layer!");
        return colladaController;
    }
	
}
