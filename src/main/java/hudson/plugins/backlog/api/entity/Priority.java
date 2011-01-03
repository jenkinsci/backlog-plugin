package hudson.plugins.backlog.api.entity;

public enum Priority {

	HIGH(2), MIDDLE(3), LOW(4);

	private int id;

	Priority(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static Priority getPriorityFromId(int id) {
		if (id == HIGH.id) {
			return HIGH;
		} else if (id == MIDDLE.id) {
			return MIDDLE;
		} else if (id == LOW.id) {
			return LOW;
		} else {
			throw new IllegalArgumentException("no priority id : " + id);
		}

	}
}
