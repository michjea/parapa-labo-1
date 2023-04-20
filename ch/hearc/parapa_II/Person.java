package ch.hearc.parapa_II;

public class Person implements Runnable {
	public static enum Role {
		READER, WRITER
	}

	private String name;
	private Document doc;
	private Role role;
	private long startingTime;
	private long durationTime;

	private long startPause;
	private long timePaused;
	private boolean paused;

	private WaitingLogger waitingLogger;
	private Timer timer;

	private String log = "";

	/**
	 * Constructor
	 * 
	 * @param name         Name of the person
	 * @param doc          Document treated by the person
	 * @param role         Role defining if the person is a reader or a writer
	 * @param startingTime Time when the person tries to access his document
	 * @param durationTime Operation duration
	 */
	public Person(String name, Document doc, Role role, long startingTime, long durationTime) {
		// Variables
		this.name = name;
		this.doc = doc;
		this.role = role;
		this.startingTime = startingTime;
		this.durationTime = durationTime;

		// Helpers
		waitingLogger = WaitingLogger.getInstance();
		timer = Timer.getInstance();
	}

	/**
	 * Runnable content
	 */
	@Override
	public void run() {
		try {

			Thread.sleep(startingTime - timer.timePassed());

			// Start waiting
			waitingLogger.addWaiting(this, timer.timePassed());

			if (role == Role.READER) {

				doc.getLock().readLock().lockInterruptibly();
				waitingLogger.removeWaiting(this, timer.timePassed());

				long startReading = timer.timePassed();
				doc.readContent();
				Thread.sleep(durationTime - (timer.timePassed() - startReading));

				waitingLogger.finished(this, timer.timePassed());
				doc.getLock().readLock().unlock();

			} else {
				doc.getLock().writeLock().lockInterruptibly();
				waitingLogger.removeWaiting(this, timer.timePassed());

				long startWriting = timer.timePassed();
				doc.setContent("Modified content : " + this.name);
				Thread.sleep(durationTime - (timer.timePassed() - startWriting));

				waitingLogger.finished(this, timer.timePassed());
				doc.getLock().writeLock().unlock();
			}
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Compute time passed in this particular runnable
	 * 
	 * @return the time passed in this runnable
	 */
	public long timePassed() {
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - timer.startTime;
		long timeInPause = currentTime - startPause;

		if (paused) {
			return timePassed - timePaused - timeInPause;
		} else {
			return timePassed - timePaused;
		}
	}

	// Getters and setters

	public String getName() {
		return name;
	}

	public Role getRole() {
		return role;
	}

	public Document getDocument() {
		return doc;
	}

	public long getStartingTime() {
		return startingTime;
	}

	public long getDurationTime() {
		return durationTime;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
}
