package com.unosquare.worldcup.strategy;

import com.unosquare.worldcup.dto.MatchWithCityDTO;
import com.unosquare.worldcup.dto.OptimisedRouteDTO;
import com.unosquare.worldcup.model.City;
import com.unosquare.worldcup.util.BuildRouteUtil;
import com.unosquare.worldcup.util.HaversineUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NearestNeighbourStrategy — YOUR TASK #3.2
 *
 * Route optimisation using nearest-neighbour heuristic.
 *
 * ============================================================
 * WHAT YOU NEED TO IMPLEMENT:
 * ============================================================
 *
 * 1. optimise() method - The nearest-neighbour algorithm:
 *    - Sort matches by kickoff date
 *    - Group matches by date
 *    - For each date, pick the match nearest to your current city
 *    - Track your current city as you process each match
 *
 * 2. validateRoute() method - Validation checks:
 *    - Must have at least 5 matches
 *    - Must visit all 3 countries (USA, Mexico, Canada)
 *    - Set feasibility, warnings, and country coverage on the route
 *
 * ============================================================
 * HELPER METHODS PROVIDED (no changes needed):
 * ============================================================
 *
 * - createEmptyRoute() - Returns an empty route with warnings
 * - buildRoute() - Builds the route from ordered matches
 * - HaversineUtil.calculateDistance() - Calculates distance between coordinates
 *
 * ============================================================
 */
@Component("nearestNeighbour")
public class NearestNeighbourStrategy implements RouteStrategy {

    private static final String STRATEGY_NAME = "nearest-neighbour";
    private static final Set<String> REQUIRED_COUNTRIES = Set.of("USA", "Mexico", "Canada");
    private static final int MINIMUM_MATCHES = 5;

    // ============================================================
    //  Nearest Neighbour Algorithm
    // ============================================================
    //
    // TODO: Implement the nearest-neighbour selection
    //
    // Steps:
    //   1. Handle empty/null matches - use createEmptyRoute()
    //   2. Sort matches by kickoff date
    //   3. Group matches by date (use Collectors.groupingBy)
    //   4. For each date (in sorted order):
    //      - If only 1 match that day, add it to orderedMatches
    //      - If multiple matches, pick the nearest to currentCity
    //   5. Track currentCity as you process each match
    //   6. Build and validate route using buildRoute() and validateRoute()
    //
    // Hints:
    //   - Use HaversineUtil.calculateDistance(lat1, lon1, lat2, lon2) for distance
    //   - Use match.getKickoff().toLocalDate() to get the date
    //   - Use Comparator for sorting
    //   - Use Collectors to group matches by date
    //
    // ============================================================

    @Override
    public OptimisedRouteDTO optimise(List<MatchWithCityDTO> matches, City originCity) {
        //defensive check first to handles cases where no matches are available
        //to prevent null pointer exceptions
        if (matches == null || matches.isEmpty()) {
            return createEmptyRoute();
        }

        //group matches by date to evaluate travel options chronologically
        Map<LocalDate, List<MatchWithCityDTO>> matchesByDate = matches.stream()
                .collect(Collectors.groupingBy(m -> m.getKickoff().toLocalDate()));

        List<LocalDate> sortedDates = matchesByDate.keySet().stream().sorted().toList();
        List<MatchWithCityDTO> orderedMatches = new ArrayList<>();
        //track current city
        City currentCity = originCity;

        for (LocalDate date : sortedDates) {
            List<MatchWithCityDTO> dailyOptions = matchesByDate.get(date);
            MatchWithCityDTO closestMatch = null;
            double shortestDistance = Double.MAX_VALUE;

            for (MatchWithCityDTO option : dailyOptions) {
                double distance = 0;
                if (currentCity != null) {
                    distance = HaversineUtil.calculateDistance(
                            currentCity.getLatitude(), currentCity.getLongitude(),
                            option.getCity().getLatitude(), option.getCity().getLongitude()
                    );
                }

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closestMatch = option;
                }
            }

            if (closestMatch != null) {
                orderedMatches.add(closestMatch);
                //update 'currentCity' to the match location so next day distance is calculated from
                //new stop, not starting location
                City nextCity = new City();
                nextCity.setId(closestMatch.getCity().getId());
                nextCity.setLatitude(closestMatch.getCity().getLatitude());
                nextCity.setLongitude(closestMatch.getCity().getLongitude());
                currentCity = nextCity;
            }
        }

        //build then validate
        OptimisedRouteDTO route = buildRoute(orderedMatches, originCity);
        validateRoute(route, orderedMatches);
        return route;
    }

    // ============================================================
    //  Validation — YOUR TASK
    // ============================================================
    //
    // TODO: Implement route validation
    //
    // Check the following constraints:
    //   1. Minimum matches - must have at least MINIMUM_MATCHES (5)
    //   2. Country coverage - must visit all REQUIRED_COUNTRIES (USA, Mexico, Canada)
    //
    // Set on the route:
    //   - route.setFeasible(true/false)
    //   - route.setWarnings(list of warning messages)
    //   - route.setCountriesVisited(list of countries)
    //   - route.setMissingCountries(list of missing countries)
    //
    // ============================================================

    /**
     * Validates route constraints (minimum matches, country coverage).
     */
    private void validateRoute(OptimisedRouteDTO route, List<MatchWithCityDTO> matches) {
        List<String> warnings = new ArrayList<>();

        //extracting country names to verify the requirement of visiting USA, Mexico and Canada
        Set<String> visitedCountries = matches.stream()
           .map(m -> m.getCity().getCountry())
           .collect(Collectors.toSet());

        List<String> missingCountries = REQUIRED_COUNTRIES.stream()
            .filter(c -> !visitedCountries.contains(c))
            .toList();

        //validation 1 - ensuring users attends minimum required 5 matches
        if (matches.size() < MINIMUM_MATCHES) {
            warnings.add("Route must include at least " + MINIMUM_MATCHES + " matches (Current: " + matches.size() + ").");
        }

        //validation 2 - ensuring country coverage across all host nations
        if (!missingCountries.isEmpty()) {
            warnings.add("Route is missing matches in: " + String.join(", ", missingCountries));
        }

        route.setCountriesVisited(new ArrayList<>(visitedCountries));
        route.setMissingCountries(missingCountries);
        route.setWarnings(warnings);

        //only feasible if it meets both rules
        route.setFeasible(missingCountries.isEmpty() && matches.size() >= MINIMUM_MATCHES);
    }
    
    // ============================================================
    //  Helper Methods (provided - no changes needed)
    // ============================================================

    /**
     * Creates an empty route with appropriate warnings.
     */
    private OptimisedRouteDTO createEmptyRoute() {
        OptimisedRouteDTO route = new OptimisedRouteDTO(new ArrayList<>(), 0, STRATEGY_NAME);
        route.setFeasible(false);
        route.setWarnings(List.of("No matches selected", "Must select at least " + MINIMUM_MATCHES + " matches"));
        route.setCountriesVisited(new ArrayList<>());
        route.setMissingCountries(List.of("USA", "Mexico", "Canada"));
        return route;
    }

    /**
     * Builds an optimised route from ordered matches, including origin city distance.
     */
    private OptimisedRouteDTO buildRoute(List<MatchWithCityDTO> orderedMatches, City originCity) {
        OptimisedRouteDTO route = BuildRouteUtil.buildRoute(orderedMatches, STRATEGY_NAME);

        // Add distance from origin city to first match
        if (originCity != null && !route.getStops().isEmpty()) {
            var firstStop = route.getStops().get(0);
            double distanceFromOrigin = HaversineUtil.calculateDistance(
                    originCity.getLatitude(), originCity.getLongitude(),
                    firstStop.getCity().getLatitude(), firstStop.getCity().getLongitude()
            );
            firstStop.setDistanceFromPrevious(distanceFromOrigin);
            route.setTotalDistance(route.getTotalDistance() + distanceFromOrigin);
        }

        return route;
    }
}