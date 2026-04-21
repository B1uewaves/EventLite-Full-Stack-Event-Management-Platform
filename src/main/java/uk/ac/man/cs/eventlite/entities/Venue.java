package uk.ac.man.cs.eventlite.entities;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "venues")
public class Venue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	private Long id;

	@NotBlank
	@Size(max = 255)
	private String name;

	@Positive
	private int capacity;

	@NotBlank
	@Pattern(regexp = ".*\\b[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}\\b.*", message = "Address must contain a valid postcode")
	private String postcode;

	@NotBlank
	@Size(max = 299) // < 300 chars
	private String address;

	@DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
	@DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
	private Double latitude;

	@DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
	@DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
	private Double longitude;

	private String description;

	@JsonIgnore
	@OneToMany(mappedBy = "venue") // Exercise 2, Task 1: relationship since -- "A venue can host multiple events"
	private Set<Event> events;

	public Venue() {
	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;

	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Set<Event> getEvents() {
		return events; // returns all events hosted at a this venue
	}
}
