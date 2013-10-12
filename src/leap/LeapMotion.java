package leap;

import java.io.IOException;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;

public class LeapMotion {
	
	public static void main(String[] args) {
		//Create a sample listener & controller
		SampleListener listener = new SampleListener(null);
		Controller controller = new Controller();
		controller.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		//Have sample listener collect events from controller
		controller.addListener(listener);
		
		//	*******
		//	*******
		//	now actually run the thingamabob
		//	*******
		//	*******
		
		// Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Remove listener when done.
        controller.removeListener(listener);
	}
	
}


