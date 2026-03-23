package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

    @Autowired
    private VenueService venueService;

    @GetMapping
    public String getAllVenues(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("venues", venueService.search(search.trim()));
            model.addAttribute("searchTerm", search.trim());
        } else {
            model.addAttribute("venues", venueService.findAll());
        }
        return "venues/index";
    }

    @GetMapping("/add")
    public String showAddVenueForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "venues/add";
    }

    @PostMapping("/add")
    public String addVenue(@Valid @ModelAttribute("venue") Venue venue,
            BindingResult result,
            Model model) {
    	
    	String normalizedName = venue.getName().trim();
    	venue.setName(normalizedName);

        // check duplicate name
        if (venueService.existsByName(normalizedName)) {
            result.rejectValue("name", "duplicate", "A venue with this name already exists");
        }


        if (result.hasErrors()) {
            return "venues/add";
        }

        venueService.save(venue);
        return "redirect:/venues";
    }
}