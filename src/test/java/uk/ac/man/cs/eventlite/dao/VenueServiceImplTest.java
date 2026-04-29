package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.man.cs.eventlite.entities.Venue;

class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    private VenueServiceImpl venueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        venueService = new VenueServiceImpl(venueRepository);
    }

    @Test
    void existsByNameShouldIgnoreCase() {
        when(venueRepository.existsByNameIgnoreCase("withworth hall")).thenReturn(true);

        boolean exists = venueService.existsByName("withworth hall");

        assertTrue(exists);
        verify(venueRepository).existsByNameIgnoreCase("withworth hall");
    }

    @Test
    void existsByNameShouldTrimLeadingAndTrailingSpaces() {
        when(venueRepository.existsByNameIgnoreCase("Withworth Hall")).thenReturn(true);

        boolean exists = venueService.existsByName("   Withworth Hall   ");

        assertTrue(exists);
        verify(venueRepository).existsByNameIgnoreCase("Withworth Hall");
    }

    @Test
    void existsByNameShouldNormaliseInternalSpaces() {
        when(venueRepository.existsByNameIgnoreCase("Withworth Hall")).thenReturn(true);

        boolean exists = venueService.existsByName("Withworth Hall");

        assertTrue(exists);
        verify(venueRepository).existsByNameIgnoreCase("Withworth Hall");
    }

    @Test
    void existsByNameShouldDetectExactDuplicate() {
        when(venueRepository.existsByNameIgnoreCase("Withworth Hall")).thenReturn(true);

        boolean exists = venueService.existsByName("Withworth Hall");

        assertTrue(exists);
        verify(venueRepository).existsByNameIgnoreCase("Withworth Hall");
    }

    @Test
    void countShouldReturnRepositoryValue() {
        when(venueRepository.count()).thenReturn(3L);

        long result = venueService.count();

        assertEquals(3L, result);
        verify(venueRepository).count();
    }
    
    @Test
    void searchShouldDelegateToRepository() {
        String name = "hall";
        venueService.search(name);
        verify(venueRepository).findByNameContainingIgnoreCase(name);
    }
    
    @Test
    void findByIdShouldCallRepository() {
        long id = 21L;
        venueService.findById(id);
        verify(venueRepository).findById(id);
    }
    
    @Test
    void deleteByIdShouldCallRepository() {
        long id = 25L;
        venueService.deleteById(id);
        verify(venueRepository).deleteById(id);
    }
    
    @Test
    void normaliseSavebeforeSavingCheck() {
        Venue venue = new Venue();
        venue.setName("  Withworth    Hall  ");

        venueService.save(venue);

        ArgumentCaptor<Venue> captor = ArgumentCaptor.forClass(Venue.class);
        verify(venueRepository).save(captor.capture());

        assertEquals("Withworth Hall", captor.getValue().getName());
    }


}