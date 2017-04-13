package me.maanuvazquez.adblocker.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Domain {

	private SimpleIntegerProperty id = new SimpleIntegerProperty();
	private SimpleStringProperty domain = new SimpleStringProperty();

	public Domain(int id, String domain) {
		this.id.set(id);
		this.domain.set(domain);
	}

	public int getId() {
		return this.id.get();
	}

	public String getDomain() {
		return this.domain.get();
	}

	public void setId(int id) {
		this.id.set(id);
	}

	public void setDomain(String domain) {
		this.domain.set(domain);
	}

}
