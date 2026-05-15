package com.unosquare.worldcup.strategy;

import com.unosquare.worldcup.dto.MatchWithCityDTO;
import com.unosquare.worldcup.dto.OptimisedRouteDTO;
import com.unosquare.worldcup.model.City;
import com.unosquare.worldcup.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NearestNeighbourStrategyTest — YOUR TASK #4
 *
 * ============================================================
 * WHAT YOU NEED TO IMPLEMENT:
 * ============================================================
 *
 * Write unit tests for the NearestNeighbourStrategy.
 * Each test has a TODO comment explaining what to test.
 *
 *
 */
class NearestNeighbourStrategyTest {

    private NearestNeighbourStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NearestNeighbourStrategy();
    }

    @Test
    void shouldReturnValidRouteForMultipleMatches() {
        // TODO: Implement this test
        //
        // Arrange: Create a list of matches across different cities and dates
        // - Create 3 cities (one in each country: USA, Mexico, Canada)
        // - Create 2 teams
        // - Create 3 matches (one per city, on different dates)
        //
        // Act: Call strategy.optimise(matches, null)
        //
        // Assert: Verify:
        // - result is not null
        // - result has 3 stops
        // - totalDistance > 0
        // - strategy = "nearest-neighbour"
        //
        City usa = new City();
        usa.setId("1");
        usa.setName("New York");
        usa.setCountry("USA");
        usa.setLatitude(40.7128);
        usa.setLongitude(-74.0060);

        City mexico = new City();
        mexico.setId("2");
        mexico.setName("Mexico City");
        mexico.setCountry("Mexico");
        mexico.setLatitude(19.4326);
        mexico.setLongitude(-99.1332);

        City canada = new City();
        canada.setId("3");
        canada.setName("Toronto");
        canada.setCountry("Canada");
        canada.setLatitude(43.6532);
        canada.setLongitude(-79.3832);

        // reuse simple test teams for all matches
        Team home = new Team();
        home.setName("Brazil");

        Team away = new Team();
        away.setName("France");

        // create matches on different days to simulate route progression
        MatchWithCityDTO match1 = new MatchWithCityDTO();
        match1.setCity(usa);
        match1.setHomeTeam(home);
        match1.setAwayTeam(away);
        match1.setKickoff(LocalDateTime.now());

        MatchWithCityDTO match2 = new MatchWithCityDTO();
        match2.setCity(mexico);
        match2.setHomeTeam(home);
        match2.setAwayTeam(away);
        match2.setKickoff(LocalDateTime.now().plusDays(1));

        MatchWithCityDTO match3 = new MatchWithCityDTO();
        match3.setCity(canada);
        match3.setHomeTeam(home);
        match3.setAwayTeam(away);
        match3.setKickoff(LocalDateTime.now().plusDays(2));

        // run optimisation strategy
        OptimisedRouteDTO result = strategy.optimise(
                List.of(match1, match2, match3),
                null
        );

        // verify route was successfully generated
        assertNotNull(result);
        assertEquals(3, result.getStops().size());
        assertTrue(result.getTotalDistance() > 0);
        assertEquals("nearest-neighbour", result.getStrategy());
    }

    @Test
    void shouldReturnEmptyRouteForEmptyMatches() {
        // TODO: Implement this test
        //
        // Arrange: Create an empty list of matches
        //
        // Act: Call strategy.optimise(emptyList, null)
        //
        // Assert: Verify:
        // - result is not null
        // - result has empty stops
        // - totalDistance = 0
        // - feasible = false
        //
        // Execute strategy with no matches selected
        OptimisedRouteDTO result = strategy.optimise(
                Collections.emptyList(),
                null
        );

        //verify fallback empty route response
        assertNotNull(result);
        assertTrue(result.getStops().isEmpty());
        assertEquals(0, result.getTotalDistance());
        assertFalse(result.isFeasible());
    }

    @Test
    void shouldReturnZeroDistanceForSingleMatch() {
        // TODO: Implement this test
        //
        // Arrange: Create a list with a single match
        // - Create 1 city
        // - Create 2 teams
        // - Create 1 match
        //
        // Act: Call strategy.optimise(matches, null)
        //
        // Assert: Verify:
        // - result is not null
        // - stops.size() = 1
        // - totalDistance = 0
        //
        City city = new City();
        city.setId("1");
        city.setName("New York");
        city.setCountry("USA");
        city.setLatitude(40.7128);
        city.setLongitude(-74.0060);

        //simple teams for testing
        Team home = new Team();
        home.setName("Argentina");

        Team away = new Team();
        away.setName("Germany");

        //single match means no travel between cities is required
        MatchWithCityDTO match = new MatchWithCityDTO();
        match.setCity(city);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setKickoff(LocalDateTime.now());

        //run optimisation
        OptimisedRouteDTO result = strategy.optimise(
                List.of(match),
                null
        );

        //verify single-stop route behaviour
        assertNotNull(result);
        assertEquals(1, result.getStops().size());
        assertEquals(0, result.getTotalDistance());
    }

}
