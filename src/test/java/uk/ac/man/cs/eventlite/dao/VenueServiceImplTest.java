package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

        boolean exists = venueService.existsByName("Withworth    Hall");

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


    
}