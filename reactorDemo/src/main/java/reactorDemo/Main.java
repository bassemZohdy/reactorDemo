package reactorDemo;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import reactor.Environment;
import reactor.rx.broadcast.Broadcaster;

public class Main {
	static {
		Environment.initialize();
	}

	public static void main(String[] args) {
		AtomicLong counter = new AtomicLong(0);
		Broadcaster<Model> b = Broadcaster.create();
		b.dispatchOn(Environment.cachedDispatcher());
		b.filter(m -> m.getSize() > 0).consume(
				s -> System.out.println(counter.getAndIncrement() + " Model : "
						+ s));
		b.filter(m -> m.getSize() < 0).consume(
				s -> System.out.println(counter.getAndIncrement() + " Model : "
						+ s));
		Random r = new Random();
		IntStream.range(0, Integer.MAX_VALUE).parallel().forEach(v -> {
			int i = r.nextInt();
			b.onNext(new Model("name" + i, r.nextLong(), r.nextDouble()));

		});

	}
}

class Model {
	private String name;
	private long size;
	private double value;

	public Model(String name, long size, double value) {
		super();
		this.name = name;
		this.size = size;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Model [name=" + name + ", size=" + size + ", value=" + value
				+ "]";
	}

}
