package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;

import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
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

        if (result.hasErrors()) {
            return "venues/add";
        }

        venueService.save(venue);
        return "redirect:/venues";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, 
                                 HttpServletRequest request, 
                                 Model model) {
        if (!request.isUserInRole("ADMINISTRATOR")) {
            return "redirect:/venues";
        }

        return venueService.findById(id).map(venue -> {
            model.addAttribute("venue", venue);
            return "venues/update";
        }).orElseThrow(() -> new VenueNotFoundException(id));
    }

    @PostMapping("/update/{id}")
    public String updateVenue(@PathVariable("id") long id,
                              @Valid @ModelAttribute("venue") Venue updatedVenue,
                              BindingResult result,
                              HttpServletRequest request,
                              Model model) {
        
        if (!request.isUserInRole("ADMINISTRATOR")) {
            return "redirect:/venues";
        }

        if (result.hasErrors()) {
            return "venues/update";
        }

        return venueService.findById(id).map(existingVenue -> {
            existingVenue.setName(updatedVenue.getName());
            existingVenue.setCapacity(updatedVenue.getCapacity());
            existingVenue.setAddress(updatedVenue.getAddress());
            existingVenue.setPostcode(updatedVenue.getPostcode());

            venueService.save(existingVenue);
            return "redirect:/venues";
        }).orElseThrow(() -> new VenueNotFoundException(id));
    }

    @ExceptionHandler(VenueNotFoundException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
        model.addAttribute("not_found_id", ex.getId());
        return "venues/not_found";
    }
}