package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.*; 
import java.util.Set;


@Entity
@Table(name = "venues")
public class Venue {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

	private Long id;

	private String name;

	private int capacity;
	
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
	
	public Set<Event> getEvents() {
	    return events; // returns all events hosted at a this venue
	}
}
