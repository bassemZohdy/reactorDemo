package disruptorDemo;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();
		// The factory for the event
		MyEventFactory factory = new MyEventFactory();
		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 1024 * 32;
		// Construct the Disruptor
		Disruptor<MyEvent> disruptor = new Disruptor<>(factory, bufferSize,
				executor);
		// Connect the handler
		disruptor.handleEventsWith(new MyEventHandler());
		// Start the Disruptor, starts all threads running
		disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<MyEvent> ringBuffer = disruptor.getRingBuffer();

		MyEventProducer producer = new MyEventProducer(ringBuffer);
		Random r = new Random();

		IntStream.range(0, Integer.MAX_VALUE).parallel().forEach(v -> {
			int i = r.nextInt();
			producer.onData("name" + i, r.nextLong(), r.nextDouble());

		});

	}

}

class MyEventProducer {
	private final RingBuffer<MyEvent> ringBuffer;

	public MyEventProducer(RingBuffer<MyEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	public void onData(String name, long size, double value) {
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			MyEvent event = ringBuffer.get(sequence); // Get the entry in the
														// Disruptor
														// for the sequence
			event.set(name, size, value); // Fill with data
		} finally {
			ringBuffer.publish(sequence);
		}
	}
}

class MyEventHandler implements EventHandler<MyEvent> {
	public void onEvent(MyEvent event, long sequence, boolean endOfBatch) {
		System.out.println(sequence + " Event: " + event);
	}
}

class MyEventFactory implements EventFactory<MyEvent> {
	public MyEvent newInstance() {
		return new MyEvent();
	}
}

class MyEvent {
	private String name;
	private long size;
	private double value;

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public double getValue() {
		return value;
	}

	public void set(String name, long size, double value) {
		this.name = name;
		this.size = size;
		this.value = value;
	}

	@Override
	public String toString() {
		return "MyEvent [name=" + name + ", size=" + size + ", value=" + value
				+ "]";
	}

}
