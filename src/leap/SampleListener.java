package leap;


import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.DrawContext;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.HandList;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.Vector;
import com.tuohy.worldwindvr.WorldWindVR;
import com.tuohy.worldwindvr.input.VRFlyView;

public class SampleListener extends Listener {
	
	SubstituteGame subgame;
	private WorldWindVR wwvr;
	
	public SampleListener(WorldWindVR wwvr) {
		this.wwvr = wwvr;
	}
	
	public void onConnect(Controller controller) {
		 System.out.println("Connected");
		    controller.enableGesture(Gesture.Type.TYPE_SWIPE);
		    controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
		    controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		    controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
	}
	
	public void onFrame(Controller controller) {
		Frame frame = controller.frame();
		System.out.println("Frame id: " + frame.id()
                + ", timestamp: " + frame.timestamp()
                + ", hands: " + frame.hands().count()
                + ", fingers: " + frame.fingers().count()
                + ", tools: " + frame.tools().count());
		
		if (!frame.hands().isEmpty()) {
			HandList hands = frame.hands();
			if (hands.count() == 2) {
				FingerList rightFingers = hands.rightmost().fingers();
				FingerList leftFingers = hands.leftmost().fingers();
				
				// if fingers are not empty, do game logic i.e. speedup
				if (!rightFingers.isEmpty() && !leftFingers.isEmpty()) {
					//is there a speedup?
					//speedup(frame, rightFingers, leftFingers);
					//get position of hands (?)
					Vector avgPos = averagePos(hands);
					
					
					/* Is this Right? */
					
					DrawContext dc = wwvr.getOculusSceneController().getDrawContext();
					
					//this is the hand angle. Angled Up = Look up, Down = look down
					if (avgPos.normalized().pitch() == 0 && avgPos.normalized().getY() == 0) {
						Angle offsetDir = new Angle(Angle.fromDegrees(avgPos.getX()));
						Angle offsetAmt = new Angle(Angle.SECOND);
						((VRFlyView) wwvr.getView()).applyWithOffset(dc, offsetDir, subgame.position, offsetAmt, subgame.elevation);
					}
					//This is the 
					
				} else {
					// there are no fingers, so fist, so stop
				}
			} else {
				//DO A BARREL ROLL. Stall...
			}
		}
		
	}
	
	//calculate pythag
	/*private double pythag(Vector pos) {
		return Math.pow(pos.getX(), 2) + Math.pow(pos.getY(), 2);
	}*/
	
	
	//Calculate avg hand position
	private Vector averagePos(HandList hands) {
		//Calculate avg hand pos
		Vector avgPos = Vector.zero();
		for (Hand hand: hands) {
			// add position of both hands
			avgPos = avgPos.plus(hand.palmPosition());
		}
		//divide vectors by number of hands
		avgPos = avgPos.divide(hands.count());
		return avgPos;
	}
	
	//Calculate avg YAW angle of hands up or down
	/*private float averageYAW(HandList hands) {
		float i = 0;
		for (Hand hand : hands) {
			i += hand.direction().yaw();
		}
		i = i/hands.count();
		return i;
	}*/
	
	/*private void speedup(Frame frame, FingerList rightFingers, FingerList leftFingers) {
		GestureList gestures = frame.gestures();
		//Loop through for each gesture
		for (Gesture gesture: gestures) {
			//check to see if gesture is a jab for super boost.
			switch (gesture.type()) {
				//if it's a jab, do SUPERWOMAN
				case TYPE_SCREEN_TAP:
					ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
					//for debug
					System.out.println("AGFJDJSGFDJSGKLFDJGSL" + "Screen Tap id: " + screenTap.id()
                               + ", " + screenTap.state()
                               + ", position: " + screenTap.position()
                               + ", direction: " + screenTap.direction());
					//	*********
					//	*********
					//	ADD SUPERMAN PART HERE WITH BOOSTS DUNNO HOW TO DO THAT
					//	*********
					//	*********
					break;

				default:
					//Other gestures arne't needed
					//System.out.println("YOU DON'T NEED THIS GESTURE GOSH");
					break;
			}
		}
	}*/
	
}
