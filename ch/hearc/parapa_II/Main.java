package ch.hearc.parapa_II;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.Scanner;

public class Main {
	/**
	 * Start a new cancellable future task to run the console reading
	 * 
	 * @param args Program parameters, not used
	 */
	public static void main(String[] args) {
		new Thread(consoleTask).start();
	}

	/**
	 * Task starting the threads and reading the console, cancelled on EXIT or when
	 * all threads are done
	 */
	private static FutureTask<String> consoleTask = new FutureTask<>(new Callable<String>() {
		@Override
		public String call() throws Exception {
			int nbDocuments = 0;
			int nbPersons = 0;

			Scanner scanner = new Scanner(System.in);

			System.out.println("----------------------------------------");
			System.out.println("|      Java Concurrency Monitoring      |");
			System.out.println("----------------------------------------");

			// ask user for number of documents (between 1 and 9)
			do {
				System.out.print("Insert number of concurrent documents (max 9) : ");
				nbDocuments = scanner.nextInt();
			} while (nbDocuments < 1 || nbDocuments > 9);

			// ask user for number of persons
			do {
				System.out.print("Insert number of readers / writers (max 9) : ");
				nbPersons = scanner.nextInt();
			} while (nbPersons < 1 || nbPersons > 9);

			// Database
			Database db = Database.getInstance();
			db.init(nbDocuments);

			// Waiting logger
			WaitingLogger waitingLogger = WaitingLogger.getInstance();

			// Create threads
			ArrayList<Person> persons = generatePopulation(db, nbPersons);

			// Start threads
			ArrayList<Thread> threads = new ArrayList<Thread>();
			for (Person person : persons) {
				Thread thread = new Thread(person);
				thread.setName(person.getName());
				thread.start();

				threads.add(thread);
			}

			// Setup waiting controller
			waitingLogger.assignConsoleFuture(consoleTask, persons);

			while (!consoleTask.isCancelled()) {

				// ask user for input
				System.out.println(" > Press ENTER to continue or type EXIT to stop the program < ");

				// read user input
				String input = scanner.nextLine();

				// if user wants to exit
				if (input.equals("EXIT")) {

					// cancel console task
					consoleTask.cancel(true);

					// interrupt all threads
					System.out.println("interrupting all threads...");

					for (Thread thread : threads) {
						thread.interrupt();
					}

				} else {
					// print next log
					waitingLogger.popNextLog();
				}
			}

			scanner.close();
			return "";
		}
	});

	/**
	 * Generate a list of person and assign them a document from the database
	 * 
	 * @param db        Database containing all documents
	 * @param nbPersons Number of persons to generate
	 * @return a list of persons
	 */
	private static ArrayList<Person> generatePopulation(Database db, int nbPersons) {
		ArrayList<Person> persons = new ArrayList<Person>();

		long minStartingTime = 0;
		long maxStartingTime = 5000;
		long minDuration = 1000;
		long maxDuration = 5000;
		double probabilityReader = 0.5f;

		for (int i = 0; i < nbPersons; i++) {
			long startTime = (long) (minStartingTime + Math.random() * (maxStartingTime - minStartingTime));
			long duration = (long) (minDuration + Math.random() * (maxDuration - minDuration));
			Person.Role role = Math.random() < probabilityReader ? Person.Role.READER : Person.Role.WRITER;

			persons.add(new Person("Thread " + (i + 1), db.getRandomDocument(), role, roundTime(startTime),
					roundTime(duration)));
		}

		return persons;
	}

	/**
	 * Round milliseconds to 100
	 * 
	 * @param time Time to round
	 * @return rounded time
	 */
	private static long roundTime(long time) {
		return time - (time % 100);
	}
}
