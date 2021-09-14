package gov.epa.warm.html;

public class PageDescriptor {

	private String id;
	private String file;
	private String title;

	public PageDescriptor(String id, String file) {
		this(id, file, id);
	}

	public PageDescriptor(String id, String file, String title) {
		this.id = id;
		this.file = file;
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public String getFile() {
		return file;
	}

	public String getTitle() {
		return title;
	}

}
