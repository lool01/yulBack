package ca.onepoint.yul.controller;

import ca.onepoint.yul.dto.AvatarDto;
import ca.onepoint.yul.dto.MapDto;
import ca.onepoint.yul.service.IAvatarService;
import ca.onepoint.yul.service.IMapService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Resource
    private IMapService iMapService;


    @Operation(summary = "Get a map by its id. 0 is wall, 1 is road, 2 is metro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the map",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MapDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Map not found",
                    content = @Content)})
    @GetMapping("/{id}")
    public MapDto findById(@PathVariable long id) throws JSONException, JsonProcessingException {
        return iMapService.getMapById(id);
    }

    @Operation(summary = "Get all map. 0 is wall, 1 is road, 2 is metro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all map",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MapDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Map not found",
                    content = @Content)})
    @GetMapping("/")
    public List<MapDto> findAll() throws JSONException, JsonProcessingException {
        return iMapService.getAllMap();
    }

}