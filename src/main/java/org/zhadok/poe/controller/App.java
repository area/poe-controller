package org.zhadok.poe.controller;
import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zhadok.poe.controller.action.macro.Macro;
import org.zhadok.poe.controller.config.ui.ConfigMappingUI;
import org.zhadok.poe.controller.lib.JInputLib;
import org.zhadok.poe.controller.util.Loggable;
import org.zhadok.poe.controller.util.Util;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

/**
 * This class shows how to use the event queue system in JInput. It will show
 * how to get the controllers, how to get the event queue for a controller, and
 * how to read and process events from the queue.
 * 
 * @author Endolf
 */
public class App implements Loggable {

	/**
	 * Setting the path to native files: 
	 * https://github.com/jinput/jinput/issues/42
	 */
	static {
		String libraryPath = new File("poe-controller-files/lib").getAbsolutePath(); 
		System.out.println("Setting net.java.games.input.librarypath=" + libraryPath);
		System.setProperty("net.java.games.input.librarypath", libraryPath);
	}
	
	public static int verbosity = Constants.DEFAULT_VERBOSITY; 
	public int getVerbosity() {
		return verbosity; 
	}
	
	private boolean running = false;
	
	/**
	 * Poll interval (in milliseconds) for controller changes
	 */
	public final int POLL_CONTROLLER_INTERVAL_MS = 20; 
	
	private List<ControllerEventListener> controllerEventListeners = new ArrayList<>(); 
	private ControllerEventListener nextEventListener = null; 	
	private int nEventsToBeSkipped = 0; 
	private boolean filterNextEventsAnalog = false; 
	public boolean filterMouseEvents = false; 
	private int nEventsSingleListener = 0; 
	
	public App() {}
	
	public void registerEventListener(ControllerEventListener listener) {
		this.controllerEventListeners.add(listener);
	}
	
	/**
	 * Unregisters all listeners of instance ControllerMapping
	 */
	public void resetControllerMappingListener() {
		this.controllerEventListeners.removeIf((listener) -> listener instanceof ControllerMapping); 
		Macro.resetMacros();
		this.registerEventListener(new ControllerMapping());
	}
	
	/**
	 * For the next n events, only this listener will be called
	 * @param nEvents How many events to listen for
	 * @param filterNextEventsAnalog Should analog events (e.g. mouse movement, joystick movement) be filtered? Note that 
	 * no other listeners will be notified
	 * @param filterMouseEvents Should mouse events be filtered? (Events with component name='x' or 'y')
	 * @param listener The listener to be notified
	 */
	public void registerForNextEvents(int nEvents, boolean filterNextEventsAnalog, boolean filterMouseEvents, ControllerEventListener listener) {
		this.nEventsSingleListener = nEvents; 
		this.filterNextEventsAnalog = filterNextEventsAnalog; 
		this.filterMouseEvents = filterMouseEvents; 
		this.nextEventListener = listener; 
	}	
	
	public void setEventsToBeSkipped(int eventsToBeSkipped) {
		this.nEventsToBeSkipped = eventsToBeSkipped; 
	}
	
	public void notifyControllerEventListeners(Event event) {
		controllerEventListeners.forEach((listener) -> listener.handleEvent(event));
	}
	
	
	public void startPolling() {
		if (this.running == true) {
			return; 
		}
		
		this.running = true;
		log(1, "Started polling for controller changes"); 
		while (running == true) {
			/* Get the available controllers */
			Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
			
			if (controllers.length == 0) {
				System.out.println("Found no controllers.");
				System.exit(0);
			}

			for (int i = 0; i < controllers.length; i++) {
				/* Remember to poll each one */
				
				if (controllers[i].poll()) {
					/* Get the controllers event queue */
					EventQueue queue = controllers[i].getEventQueue();

					/* Create an event object for the underlying plugin to populate */
					Event event = new Event();

					/* For each object in the queue */
					while (queue.getNextEvent(event)) {
						this.handleEvent(event);
					}
				}
			}

			/*
			 * Sleep for 20 milliseconds, in here only so the example doesn't
			 * thrash the system.
			 */
			try {
				Thread.sleep(POLL_CONTROLLER_INTERVAL_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopPolling() {
		if (this.running == true) {
			log(1, "Stopped polling for controller changes"); 
			this.running = false;
		}
	}
	
	public boolean isEventMouseMovement(Event event) {
		return "net.java.games.input.RawMouse$Axis".equals(event.getComponent().getClass().getName()); 
	}
	
	private long lastEventTimestamp = -1; 
	private void handleEvent(Event event) {
		if (isEventMouseMovement(event)) {
			return; 
		}
		
		if (getVerbosity() >= 3) {
			StringBuffer buffer = new StringBuffer();
			//buffer.append(event.getNanos()).append(", ");
			Component comp = event.getComponent();
			buffer.append(comp.getName());
			buffer.append(" (id=" + comp.getIdentifier() + ")");
			buffer.append(": "); 
			float value = event.getValue();
			
			/*
			 * Check the type of the component and display an
			 * appropriate value
			 */
			if (comp.isAnalog()) {
				buffer.append(value);
			} else {
				if (value > 0) {
					buffer.append("On (value=" + value + ")");
				} else {
					buffer.append("Off (value=" + value + ")");
				}
			}
			long timestamp = event.getNanos(); 
			
			long diffMS = (timestamp - lastEventTimestamp) / 1000000; 
			buffer.append(". ").append(diffMS).append("ms since last event"); 
			this.lastEventTimestamp = timestamp; 
			
			log(3, buffer.toString());
		}
		
		if (nEventsToBeSkipped > 0) {
			nEventsToBeSkipped--; 
			return; 
		}
		if (filterNextEventsAnalog == true && event.getComponent().isAnalog() == true) {
			return; 
		}
		
		if (this.nextEventListener != null && nEventsSingleListener > 0) {
			// If a single event listener is registered for next event (e.g. UI)
			// Only notify that listener
			this.nextEventListener.handleEvent(event);
			this.nEventsSingleListener--; 
			
			if (nEventsSingleListener == 0) {
				this.nextEventListener = null; 
				this.filterNextEventsAnalog = false; 
				this.filterMouseEvents = false; 
			}
		} else {
			this.notifyControllerEventListeners(event); 
		}
	}
	
	private ConfigMappingUI startConfigMappingUI() {
		ConfigMappingUI window = new ConfigMappingUI(this);
		return window; 
	}
	
	
	/**
	 * @param args
	 * @throws AWTException
	 * @throws IOException
	 */
	public static void main(String[] args) throws AWTException, IOException {
		Util.ensureProjectDirExists(); 
		Loggable.writeLogsToFile(Constants.FILE_LOG); 
		
		Util.getJavaRuntime().forEach(detail -> System.out.println(detail)); 
		
		JInputLib lib = new JInputLib();
		lib.prepare(); 
		
		App app = new App();
		if (System.getProperty("verbosity") != null) {
			App.verbosity = Integer.valueOf(System.getProperty("verbosity")); 
			app.log(0, "Setting verbosity to " + verbosity);
		}
		app.resetControllerMappingListener();
		ConfigMappingUI window = app.startConfigMappingUI(); 
		app.registerEventListener(window);
		
		if (Util.isJava32Bit()) {
			app.log(1, "WARNING: You appear to be using 32-bit Java. If the program crashes please try " + 
					   "uninstalling 32-bit Java and installing 64-bit Java."); 
		}
		
		app.startPolling(); 
	}

	
	
	
	
	
}
