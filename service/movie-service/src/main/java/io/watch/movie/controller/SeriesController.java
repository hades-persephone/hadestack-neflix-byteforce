package io.watch.movie.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.watch.movie.dto.request.SeriesRequest;
import io.watch.movie.dto.response.SeriesResponse;
import io.watch.movie.response.ApiResponseEntity;
import io.watch.movie.response.ResponseBuilder;
import io.watch.movie.service.SeriesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final ResponseBuilder responseBuilder;
    private final SeriesService seriesService;

    @Operation(summary = "Create a new series", description = "Creates a new series with seasons and episodes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Series created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ApiResponseEntity<Object>> createSeries(@Valid @RequestBody SeriesRequest request, HttpServletRequest req) {
        seriesService.createSeries(request);
        return responseBuilder.successMessage("Created a new movie", req);

    }

    @Operation(summary = "Get a series by ID", description = "Retrieves a series with its seasons and episodes by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Series retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Series not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<SeriesResponse>> getSeries(@PathVariable UUID id, HttpServletRequest req) {
        return responseBuilder.success(seriesService.getSeries(id), req);
    }

    @Operation(summary = "Get all series", description = "Retrieves a list of all available series")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Series list retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ApiResponseEntity<List<SeriesResponse>>> getAllSeries(HttpServletRequest req) {
        return responseBuilder.success(seriesService.getAllSeries(), req);
    }

    @Operation(summary = "Update a series", description = "Updates an existing series with new data including seasons and episodes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Series updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Series not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<SeriesResponse>> updateSeries(
            @PathVariable UUID id,
            @Valid @RequestBody SeriesRequest request,
            HttpServletRequest req) {
        return responseBuilder.success(seriesService.updateSeries(id, request), req);
    }

    @Operation(summary = "Delete a series", description = "Soft deletes a series by marking it as unavailable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Series deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Series not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseEntity<Object>> deleteSeries(@PathVariable UUID id, HttpServletRequest req) {
        seriesService.deleteSeries(id);
        return responseBuilder.successMessage("Created a new movie", req);
    }

    @Operation(summary = "Increment view count", description = "Increments the view count of a series")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "View count incremented successfully"),
            @ApiResponse(responseCode = "404", description = "Series not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/increment-view")
    public ResponseEntity<ApiResponseEntity<Object>> incrementViewCount(@PathVariable UUID id, HttpServletRequest req) {
        seriesService.incrementViewCount(id);
        return responseBuilder.successMessage("Created a new movie", req);
    }
}
