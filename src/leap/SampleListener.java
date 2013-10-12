package leap;


import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.animation.AngleAnimator;
import gov.nasa.worldwind.animation.AnimationController;
import gov.nasa.worldwind.animation.AnimationSupport;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.animation.MoveToPositionAnimator;
import gov.nasa.worldwind.animation.RotateToAngleAnimator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.ViewPropertyAccessor;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.firstperson.FlyToFlyViewAnimator;
import gov.nasa.worldwind.view.firstperson.FlyViewLimits;
import gov.nasa.worldwind.view.orbit.OrbitViewPropertyAccessor;

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
	
	double cameraTranslationSpeed = 7.0;
	
    protected static final String VIEW_ANIM_HEADING = "ViewAnimHeading";
    protected static final String VIEW_ANIM_PITCH = "ViewAnimPitch";
    protected static final String VIEW_ANIM_ROLL = "ViewAnimRoll";
    protected static final String VIEW_ANIM_POSITION = "ViewAnimPosition";
    protected static final String VIEW_ANIM_PAN = "ViewAnimPan";
    protected static final String VIEW_ANIM_APP = "ViewAnimApp";

    protected static final String ACTION_RESET_PITCH = "ResetPitch";

    protected static final double DEFAULT_MOUSE_ROTATE_MIN_VALUE = 0.014; // Speed in degrees per mouse movement
    protected static final double DEFAULT_MOUSE_ROTATE_MAX_VALUE = 0.018; // Speed in degrees per mouse movement

    // Keyboard/Action calibration values for extensible view/navigation support
    //protected static final double DEFAULT_KEY_HORIZONTAL_TRANSLATE_MIN_VALUE = 10;
    protected static final double DEFAULT_KEY_TRANSLATE_SMOOTHING_VALUE = .9;
    protected static final double DEFAULT_KEY_HORIZONTAL_TRANSLATE_MAX_VALUE = 1000000.0;
    //NOTE: This is the parameter that we changed for VR, because movement was too fast when next to the ground (originally 100?)
    //We can also control movement speed with the 'speed' field in this class (WARNING: manipulating these values appears to make
    //movement unpredictable, camera does not go in direction of heading back and forwards)
    protected static final double DEFAULT_KEY_HORIZONTAL_TRANSLATE_MIN_VALUE = 100;

    protected static final double DEFAULT_KEY_HORIZONTAL_TRANSLATE_MIN_VALUE_SLOW = 1;
    protected static final double DEFAULT_KEY_HORIZONTAL_TRANSLATE_MAX_VALUE_SLOW = 100000.0;

    protected static double DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MIN_VALUE = 5;
    protected static double DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MAX_VALUE = 50000.0;
    protected static final double DEFAULT_MOUSE_VERTICAL_TRANSLATE_MIN_VALUE = 1;
        // Speed in log-meters per mouse movement
    protected static final double DEFAULT_MOUSE_VERTICAL_TRANSLATE_MAX_VALUE = 30000;
        // Speed in log-meters per mouse movement

    protected static final double DEFAULT_KEY_VERTICAL_TRANSLATE_MIN_VALUE = 5;
    protected static final double DEFAULT_KEY_VERTICAL_TRANSLATE_MAX_VALUE = 5000;

    protected static final double DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE_MIN_OSX = 10;
        // Speed in log-meters per wheel movement
    protected static final double DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE_MAX_OSX = 900000;
        // Speed in log-meters per wheel movement
    protected static final double DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE_MIN = 100;
        // Speed in log-meters per wheel movement
    protected static final double DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE_MAX = 100000;
        // Speed in log-meters per wheel movement

	
	SubstituteGame subgame;
	private WorldWindVR wwvr;
	

    AnimationController uiAnimControl = new AnimationController();
    AnimationController gotoAnimControl = new AnimationController();

	private Robot robot;
	private double width;
	private double height;
	private boolean moving = false;
	
	public SampleListener(WorldWindVR wwvr) {
		this.wwvr = wwvr;
		try {
			this.robot = new Robot();
			//this.robot.setAutoDelay(600);
			this.width = java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
			this.height =  java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		if (!frame.hands().isEmpty()) {
			HandList hands = frame.hands();
			
			if (hands.count() == 2) {
				FingerList rightFingers = hands.rightmost().fingers();
				FingerList leftFingers = hands.leftmost().fingers();
				
				// if fingers are not empty, do game logic i.e. speedup
				if (frame.fingers().count() > 3) {
					if(!moving) {
						this.robot.keyPress(KeyEvent.VK_W);
					}
					
					Hand frontmost = hands.frontmost();
					Hand rightmost = hands.rightmost();
					Hand leftmost = hands.leftmost();

					if (frontmost.equals(rightmost) && Math.abs(hands.frontmost().palmPosition().getZ() - hands.leftmost().palmPosition().getZ()) > 40) {
						//turn right.xwwwwwwwwwwwwwwwwwwwwwwwwwwwww
						this.robot.mouseMove((int) (this.width/2 - 100), (int) (this.height/2));
						System.out.println("WORK DAMMIT");
					} else if (frontmost.equals(leftmost) && Math.abs(hands.frontmost().palmPosition().getZ() - hands.rightmost().palmPosition().getZ()) > 40) {
						//turn left
						this.robot.mouseMove((int) (this.width/2 + 100), (int) (this.height/2));
						System.out.println("WORK ALREADY");
					}

					
				} else {
					// there are no fingers, so fist, so stop
					if(moving) {
						this.robot.keyRelease(KeyEvent.VK_W);
					}
				}
			} else {
				//DO A BARREL ROLL. Stall...
				if(moving) {
					this.robot.keyRelease(KeyEvent.VK_W);
				}
			}
			//this.robot.mouseMove((int) (this.width/2), 0);
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
	
	
	

    protected void onMoveTo(Position focalPosition, ViewInputAttributes.DeviceAttributes deviceAttributes,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {
        BasicFlyView view = (BasicFlyView) wwvr.view;
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        // We're treating a speed parameter as smoothing here. A greater speed results in greater smoothing and
        // slower response. Therefore the min speed used at lower altitudes ought to be *greater* than the max
        // speed used at higher altitudes.
        double smoothing = this.getScaleValueElevation(deviceAttributes, actionAttribs);
        if (!actionAttribs.isEnableSmoothing())
            smoothing = 0.0;

        Vec4 currentLookAtPt = view.getCenterPoint();
        if (currentLookAtPt == null)
        {
            currentLookAtPt = view.

                getGlobe().computePointFromPosition(focalPosition);
        }

        Vec4 currentEyePt = view.getEyePoint();
        double distanceToSurface = currentEyePt.distanceTo3(currentLookAtPt);
        Vec4 lookDirection = currentEyePt.subtract3(currentLookAtPt).normalize3();
        Vec4 newLookAtPt = view.getGlobe().computePointFromPosition(focalPosition);
        Vec4 flyToPoint = newLookAtPt.add3(lookDirection.multiply3(distanceToSurface));

        Position newPosition = view.getGlobe().computePositionFromPoint(flyToPoint);

        ViewUtil.ViewState viewCoords = view.getViewState(newPosition, focalPosition);

        this.stopAnimators();
        this.gotoAnimControl.put(VIEW_ANIM_HEADING,
            new RotateToAngleAnimator(
                view.getHeading(), viewCoords.getHeading(), smoothing,
                ViewPropertyAccessor.createHeadingAccessor(view)));
        this.gotoAnimControl.put(VIEW_ANIM_PITCH,
            new RotateToAngleAnimator(
                view.getPitch(), viewCoords.getPitch(), smoothing,
                ViewPropertyAccessor.createPitchAccessor(view)));

        double elevation = ((FlyViewLimits)
            view.getViewPropertyLimits()).limitEyeElevation(
            newPosition, view.getGlobe());
        if (elevation != newPosition.getElevation())
        {
            newPosition = new Position(newPosition, elevation);
        }
        this.gotoAnimControl.put(VIEW_ANIM_POSITION,
            new MoveToPositionAnimator(
                view.getEyePosition(), newPosition, smoothing,
                ViewPropertyAccessor.createEyePositionAccessor(view)));

        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void onMoveTo(Position focalPosition, ViewInputAttributes.ActionAttributes actionAttribs)
    {

    }

    protected void onHorizontalTranslateAbs(Angle latitudeChange, Angle longitudeChange,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {

    }

    protected void onHorizontalTranslateRel(double forwardInput, double sideInput,
        double totalForwardInput, double totalSideInput,
        ViewInputAttributes.DeviceAttributes deviceAttributes,
        ViewInputAttributes.ActionAttributes actionAttributes)
    {
    	//Changed for VR application: negating the side input means that the camera
    	//actually moves int he direction of the arrow key that was pressed
    	sideInput = -sideInput;
    	
        Angle forwardChange;
        Angle sideChange;

        this.stopGoToAnimators();
        if (actionAttributes.getMouseActions() != null)
        {
            forwardChange = Angle.fromDegrees(-totalForwardInput
                * getScaleValueElevation(deviceAttributes, actionAttributes));
            sideChange = Angle.fromDegrees(totalSideInput
                * getScaleValueElevation(deviceAttributes, actionAttributes));
        }
        else
        {
            forwardChange = Angle.fromDegrees(
                forwardInput * cameraTranslationSpeed * getScaleValueElevation(deviceAttributes, actionAttributes));
            sideChange = Angle.fromDegrees(
                sideInput * cameraTranslationSpeed * getScaleValueElevation(deviceAttributes, actionAttributes));
        }
        onHorizontalTranslateRel(forwardChange, sideChange, actionAttributes);
    }

    protected void onHorizontalTranslateRel(Angle forwardChange, Angle sideChange,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = wwvr.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (forwardChange.equals(Angle.ZERO) && sideChange.equals(Angle.ZERO))
        {
            return;
        }

        if (view instanceof BasicFlyView)
        {

            Vec4 forward = view.getForwardVector();
            Vec4 up = view.getUpVector();
            Vec4 side = forward.transformBy3(Matrix.fromAxisAngle(Angle.fromDegrees(90), up));

            forward = forward.multiply3(forwardChange.getDegrees());
            side = side.multiply3(sideChange.getDegrees());
            Vec4 eyePoint = view.getEyePoint();
            eyePoint = eyePoint.add3(forward.add3(side));
            Position newPosition = view.getGlobe().computePositionFromPoint(eyePoint);

            this.setEyePosition(this.uiAnimControl, view, newPosition, actionAttribs);
            view.firePropertyChange(AVKey.VIEW, null, view);
        }
    }

    protected void onResetHeading(ViewInputAttributes.ActionAttributes actionAttribs)
    {

        View view = wwvr.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }
        double smoothing = actionAttribs.getSmoothingValue();
        if (!(actionAttribs.isEnableSmoothing() && true))
            smoothing = 0.0;
        this.gotoAnimControl.put(VIEW_ANIM_HEADING,
            new RotateToAngleAnimator(
                view.getHeading(), Angle.ZERO, smoothing,
                ViewPropertyAccessor.createHeadingAccessor(view)));
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void onResetPitch(ViewInputAttributes.ActionAttributes actionAttribs)
    {

        View view = wwvr.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }
        double smoothing = actionAttribs.getSmoothingValue();
        if (!(actionAttribs.isEnableSmoothing() && true))
            smoothing = 0.0;
        this.gotoAnimControl.put(VIEW_ANIM_PITCH,
            new RotateToAngleAnimator(
                view.getPitch(), Angle.POS90, smoothing,
                ViewPropertyAccessor.createPitchAccessor(view)));
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void onResetHeadingPitchRoll(ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = wwvr.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }
        double smoothing = 0.95;
        this.gotoAnimControl.put(VIEW_ANIM_HEADING,
            new RotateToAngleAnimator(
                view.getHeading(), Angle.ZERO, smoothing,
                ViewPropertyAccessor.createHeadingAccessor(view)));
        this.gotoAnimControl.put(VIEW_ANIM_PITCH,
            new RotateToAngleAnimator(
                view.getPitch(), Angle.POS90, smoothing,
                ViewPropertyAccessor.createPitchAccessor(view)));
        this.gotoAnimControl.put(VIEW_ANIM_ROLL,
            new RotateToAngleAnimator(
                view.getPitch(), Angle.ZERO, smoothing,
                ViewPropertyAccessor.createRollAccessor(view)));
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void onRotateView(double headingInput, double pitchInput,
        double totalHeadingInput, double totalPitchInput,
        ViewInputAttributes.DeviceAttributes deviceAttributes,
        ViewInputAttributes.ActionAttributes actionAttributes)
    {

        Angle headingChange;
        Angle pitchChange;
        this.stopGoToAnimators();
        headingChange = Angle.fromDegrees(
            totalHeadingInput * getScaleValueElevation(deviceAttributes, actionAttributes));
        pitchChange = Angle.fromDegrees(
            totalPitchInput * getScaleValueElevation(deviceAttributes, actionAttributes));
        onRotateView(headingChange, pitchChange, actionAttributes);
    }

    protected void onRotateView(Angle headingChange, Angle pitchChange,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {

        View view = wwvr.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (view instanceof BasicFlyView)
        {
            BasicFlyView flyView = (BasicFlyView) view;
            this.setPitch(flyView, this.uiAnimControl, flyView.getPitch().add(pitchChange),
                actionAttribs);
            this.setHeading(flyView, this.uiAnimControl, flyView.getHeading().add(headingChange),
                actionAttribs);
            view.firePropertyChange(AVKey.VIEW, null, view);
        }
    }

    /**
     * Called when the roll changes due to user input.
     *
     * @param rollInput        Change in roll.
     * @param deviceAttributes Attributes of the input device.
     * @param actionAttributes Action that caused the change.
     */
    protected void onRoll(double rollInput, ViewInputAttributes.DeviceAttributes deviceAttributes,
        ViewInputAttributes.ActionAttributes actionAttributes)
    {
        Angle rollChange;
        this.stopGoToAnimators();

        rollChange = Angle.fromDegrees(rollInput * getScaleValueElevation(deviceAttributes, actionAttributes));

        this.onRoll(rollChange, actionAttributes);
    }

    /**
     * Called when the roll changes due to user input.
     *
     * @param rollChange    Change in roll.
     * @param actionAttribs Action that caused the change.
     */
    protected void onRoll(Angle rollChange, ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = wwvr.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (view instanceof BasicFlyView)
        {
            BasicFlyView flyView = (BasicFlyView) view;
            this.setRoll(flyView, this.uiAnimControl, flyView.getRoll().add(rollChange), actionAttribs);

            view.firePropertyChange(AVKey.VIEW, null, view);
        }
    }

    protected void onVerticalTranslate(double translateChange, double totalTranslateChange,
        ViewInputAttributes.DeviceAttributes deviceAttributes,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {
        this.stopGoToAnimators();
        double elevChange = -(totalTranslateChange * getScaleValueElevation(deviceAttributes, actionAttribs));
        View view = wwvr.getView();
        Position position = view.getEyePosition();
        Position newPos = new Position(position, position.getElevation() + (elevChange));
        this.setEyePosition(uiAnimControl, view, newPos, actionAttribs);

        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void onVerticalTranslate(double translateChange,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {

    }

    public void apply()
    {
       // super.apply();

        View view = wwvr.getView();
        if (view == null)
        {
            return;
        }
        if (this.gotoAnimControl.stepAnimators())
        {
            view.firePropertyChange(AVKey.VIEW, null, view);
        }
        if (this.uiAnimControl.stepAnimators())
        {
            view.firePropertyChange(AVKey.VIEW, null, view);
        }
    }

    protected void handleViewStopped()
    {
        this.stopAnimators();
    }

    protected void setHeading(View view,
        AnimationController animControl,
        Angle heading, ViewInputAttributes.ActionAttributes attrib)
    {
        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && true))
            smoothing = 0.0;

        AngleAnimator angleAnimator = new RotateToAngleAnimator(
            view.getHeading(), heading, smoothing,
            ViewPropertyAccessor.createHeadingAccessor(view));
        animControl.put(VIEW_ANIM_HEADING, angleAnimator);
    }

    protected void setPitch(View view,
        AnimationController animControl,
        Angle pitch, ViewInputAttributes.ActionAttributes attrib)
    {
        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && true))
            smoothing = 0.0;

        AngleAnimator angleAnimator = new RotateToAngleAnimator(
            view.getPitch(), pitch, smoothing,
            ViewPropertyAccessor.createPitchAccessor(view));
        animControl.put(VIEW_ANIM_PITCH, angleAnimator);
    }

    /**
     * Set the roll in a view.
     *
     * @param view        View to modify.
     * @param animControl Animator controller for the view.
     * @param roll        new roll value.
     * @param attrib      action that caused the roll to change.
     */
    protected void setRoll(View view,
        AnimationController animControl,
        Angle roll, ViewInputAttributes.ActionAttributes attrib)
    {
        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && true))
            smoothing = 0.0;

        AngleAnimator angleAnimator = new RotateToAngleAnimator(
            view.getRoll(), roll, smoothing,
            ViewPropertyAccessor.createRollAccessor(view));
        animControl.put(VIEW_ANIM_ROLL, angleAnimator);
    }

    protected void setEyePosition(AnimationController animControl, View view, Position position,
        ViewInputAttributes.ActionAttributes attrib)
    {

        MoveToPositionAnimator posAnimator = (MoveToPositionAnimator)
            animControl.get(VIEW_ANIM_POSITION);

        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && true))
            smoothing = 0.0;

        if (smoothing != 0.0)
        {

            double elevation = ((FlyViewLimits)
                view.getViewPropertyLimits()).limitEyeElevation(
                position, view.getGlobe());
            if (elevation != position.getElevation())
            {
                position = new Position(position, elevation);
            }
            if (posAnimator == null)
            {
                posAnimator = new MoveToPositionAnimator(
                    view.getEyePosition(), position, smoothing,
                    OrbitViewPropertyAccessor.createEyePositionAccessor(view));
                animControl.put(VIEW_ANIM_POSITION, posAnimator);
            }
            else
            {
                posAnimator.setEnd(position);
                posAnimator.start();
            }
        }
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    public void goTo(Position lookAtPos, double distance)
    {

        Globe globe = wwvr.getView().getGlobe();
        BasicFlyView view = (BasicFlyView) wwvr.getView();

        Position lookFromPos = new Position(lookAtPos,
            globe.getElevation(lookAtPos.getLatitude(), lookAtPos.getLongitude()) + distance);

        // TODO: scale on mid-altitude?
        final long MIN_LENGTH_MILLIS = 4000;
        final long MAX_LENGTH_MILLIS = 16000;
        long timeToMove = AnimationSupport.getScaledTimeMillisecs(
            view.getEyePosition(), lookFromPos,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);
        FlyToFlyViewAnimator panAnimator = FlyToFlyViewAnimator.createFlyToFlyViewAnimator(view,
            view.getEyePosition(), lookFromPos,
            view.getHeading(), Angle.ZERO,
            view.getPitch(), Angle.ZERO,
            view.getEyePosition().getElevation(), lookFromPos.getElevation(),
            timeToMove, WorldWind.ABSOLUTE);

        this.gotoAnimControl.put(VIEW_ANIM_PAN, panAnimator);

        wwvr.getView().firePropertyChange(AVKey.VIEW, null, wwvr.getView());
    }

    public void lookAt(Position lookAtPos, long timeToMove)
    {
        BasicFlyView view = (BasicFlyView) wwvr.getView();
        Vec4 lookDirection;
        double distanceToSurface;
        Vec4 currentLookAtPt = view.getCenterPoint();
        Position newPosition;
        if (currentLookAtPt == null)
        {
            view.getGlobe().computePointFromPosition(lookAtPos);
            double elevAtLookAtPos = view.getGlobe().getElevation(
                lookAtPos.getLatitude(), lookAtPos.getLongitude());
            newPosition = new Position(lookAtPos, elevAtLookAtPos + 10000);
        }
        else
        {
            Vec4 currentEyePt = view.getEyePoint();
            distanceToSurface = currentEyePt.distanceTo3(currentLookAtPt);
            lookDirection = currentLookAtPt.subtract3(currentEyePt).normalize3();
            Vec4 newLookAtPt = view.getGlobe().computePointFromPosition(lookAtPos);
            Vec4 flyToPoint = newLookAtPt.add3(lookDirection.multiply3(-distanceToSurface));
            newPosition = view.getGlobe().computePositionFromPoint(flyToPoint);
        }

        ViewUtil.ViewState viewCoords = view.getViewState(newPosition, lookAtPos);

        FlyToFlyViewAnimator panAnimator = FlyToFlyViewAnimator.createFlyToFlyViewAnimator(view,
            view.getEyePosition(), newPosition,
            view.getHeading(), viewCoords.getHeading(),
            view.getPitch(), viewCoords.getPitch(),
            view.getEyePosition().getElevation(), viewCoords.getPosition().getElevation(),
            timeToMove, WorldWind.ABSOLUTE);

        this.gotoAnimControl.put(VIEW_ANIM_PAN, panAnimator);
        wwvr.getView().firePropertyChange(AVKey.VIEW, null, wwvr.getView());

        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    public void stopAnimators()
    {
        this.stopGoToAnimators();
        this.stopUserInputAnimators();
    }

    public boolean isAnimating()
    {
        return (this.uiAnimControl.hasActiveAnimation() || this.gotoAnimControl.hasActiveAnimation());
    }

    public void addAnimator(Animator animator)
    {
        this.gotoAnimControl.put(VIEW_ANIM_APP, animator);
    }

    protected void stopGoToAnimators()
    {
        // Explicitly stop all 'go to' animators, then clear the data structure which holds them. If we remove an
        // animator from this data structure without invoking stop(), the animator has no way of knowing it was forcibly
        // stopped. An animator's owner - likely an application object other - may need to know if an animator has been
        // forcibly stopped in order to react correctly to that event.
        this.gotoAnimControl.stopAnimations();
        this.gotoAnimControl.clear();
    }

    protected void stopUserInputAnimators()
    {
        // Explicitly stop all 'ui' animator, then clear the data structure which holds them. If we remove an animator
        // from this data structure without invoking stop(), the animator has no way of knowing it was forcibly stopped.
        // Though applications cannot access the 'ui' animator data structure, stopping the animators here is the correct
        // action.
        this.uiAnimControl.stopAnimations();
        this.uiAnimControl.clear();
    }
    
    protected double getScaleValueElevation(
            ViewInputAttributes.DeviceAttributes deviceAttributes, ViewInputAttributes.ActionAttributes actionAttributes)
        {
            View view = this.wwvr.getView();
            if (view == null)
            {
                return 0.0;
            }

            double[] range = actionAttributes.getValues();

            Position eyePos = view.getEyePosition();
            double radius = this.wwvr.wwd.getModel().getGlobe().getRadius();
            double surfaceElevation = this.wwvr.wwd.getModel().getGlobe().getElevation(eyePos.getLatitude(),
                eyePos.getLongitude());
            double t = getScaleValue(range[0], range[1],
                eyePos.getElevation() - surfaceElevation, 3.0 * radius, true);
             t *= deviceAttributes.getSensitivity();

            return t;
        }
    
    protected double getScaleValue(double minValue, double maxValue,
            double value, double range, boolean isExp)
        {
            double t = value / range;
            t = t < 0 ? 0 : (t > 1 ? 1 : t);
            if (isExp)
            {
                t = Math.pow(2.0, t) - 1.0;
            }
            return(minValue * (1.0 - t) + maxValue * t);
        }
	
}
