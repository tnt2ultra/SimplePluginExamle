package main;

public interface Plugin {
	default void initialize() {
		System.out.println("Initialized " + this.getClass().getName());
	}

	default String name() {
		return getClass().getSimpleName();
	}
}