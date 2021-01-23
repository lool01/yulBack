package ca.onepoint.yul.controller;

import ca.onepoint.yul.dto.MapDto;
import ca.onepoint.yul.dto.PositionDto;
import ca.onepoint.yul.dto.SquareDto;
import ca.onepoint.yul.service.IMapService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Resource
    private IMapService iMapService;


    @Operation(summary = "Get a map by its id. 0 is wall, 1 is road, 2 is metro, 3 is shop or company, 4 to finish")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the map",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MapDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Map not found",
                    content = @Content)})
    @CrossOrigin
    @GetMapping("/{id}")
    public MapDto findById(@PathVariable long id) throws JSONException, JsonProcessingException {
        return iMapService.getMapById(id);
    }

    @Operation(summary = "Get all map. 0 is wall, 1 is road, 2 is metro, 3 is shop or company, 4 to finish")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all map",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MapDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Map not found",
                    content = @Content)})
    @CrossOrigin
    @GetMapping("/")
    public List<MapDto> findAll() throws JSONException, JsonProcessingException {
        return iMapService.getAllMap();
    }

    @Operation(summary = "Compute the absolute best path something could take to get to the target location !")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It worked !",
                            content = {@Content(mediaType = "application/json",
                                schema = @Schema(implementation = PositionDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid positions supplied",
                    content = @Content),
    })
    @CrossOrigin
    @GetMapping("/path-finding/{fromX}/{fromY}/{toX}/{toY}")
    public List<PositionDto> pathFinding(
            @PathVariable int fromX, @PathVariable int fromY,
            @PathVariable int toX, @PathVariable int toY) throws Exception {

        // Fetch the current map
        MapDto map = null;
        try {
            map = iMapService.getAllMap().get(0);
        }catch (Exception e){
            System.err.println("error !: " + e.getMessage());
            throw new Exception("ERROR !");
        }

        // Fetch the squares in the map
        SquareDto[][] squares = map.getSquares();
        int h = squares.length;
        int w = squares[0].length;

        // Verify that input is OK
        if(fromX < 0 || fromY < 0 || toX < 0 || toY < 0){
            throw new Exception("Invalid positions < 0");
        }
        if(fromX >= w || toX >= w || fromY > h || toY > h){
            throw new Exception("Invalid positions > map width");
        }

        // Compute the freeSquares 2D array: 1 = can go through, 0 = obstacle
        int[][] freeSquares = new int[w][h];

        for(int x = 0; x < w ; x++){
            for(int y = 0; y < h; y++){
                freeSquares[x][y] = squares[x][y].getValue() == 1 ? 1 : 0; // On the map 1 represent a road
            }
        }

        // Verify that from and to position are on roads
        if(freeSquares[fromX][fromY] != 1 || freeSquares[toX][toY] != 1){
            throw new Exception("From or To positions not on road !");
        }

        for(int[] row : freeSquares){
            for(int item : row){
                System.out.print(item + " ");
            }
            System.out.println();
        }

        return this.aStar(freeSquares, new PositionDto(fromX, fromY), new PositionDto(toX, toY));
    }

    /**
     * A* implementation, please read https://en.wikipedia.org/wiki/A*_search_algorithm
     */
    private List<PositionDto> aStar(int[][] freeSquares, PositionDto from, PositionDto to){
        ArrayList<PositionDto> path = new ArrayList<>();

        HashSet<PositionDto> openSet = new HashSet<>();

        // Initialize gScore
        HashMap<PositionDto, Integer> gScore = new HashMap<>();
        for(int x = 0; x < freeSquares.length ; x++){
            for(int y = 0; y < freeSquares[0].length ; y++){
                PositionDto position = new PositionDto(x, y);
                gScore.put(position, Integer.MAX_VALUE);
            }
        }
        gScore.put(from, 0);

        // Initialize fScore


        return path;
    }

}
