package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.*; 

@Entity
@Table(name = "venues")
public class Venue {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

	private Long id;

	private String name;

	private int capacity;

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
}
