package ca.onepoint.yul.controller;

import ca.onepoint.yul.dto.AvatarDto;
import ca.onepoint.yul.dto.MapDto;
import ca.onepoint.yul.dto.PositionDto;
import ca.onepoint.yul.dto.SquareDto;
import ca.onepoint.yul.service.IAvatarService;
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
import java.util.*;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Resource
    private IMapService iMapService;

    @Resource
    private IAvatarService iAvatarService;


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
    @GetMapping("/path-finding/{objectType}/{fromX}/{fromY}/{toX}/{toY}")
    public List<PositionDto> pathFinding(
            @PathVariable int objectType,
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

        List<AvatarDto> avatars = iAvatarService.getAllAvatars();

        // Compute the freeSquares 2D array: 1 = can go through, 0 = obstacle
        int[][] freeSquares = new int[w][h];

        for(int x = 0; x < w ; x++){
            for(int y = 0; y < h; y++){
                freeSquares[y][x] = (squares[x][y].getValue() == 1 || squares[x][y].getValue() == 3 || squares[x][y].getValue() == 4)  ? 1 : 0; // On the map 1 represent a road, 3 = restaurant or business
            }
        }

        for (AvatarDto avatar: avatars){
            freeSquares[avatar.getX()][avatar.getY()] = 0;
        }

        for(int x = 0; x < 30; x++){
            for(int y = 0; y< 30; y++){
                System.out.print(squares[x][y].getValue() + " ");
            }
            System.out.println();
        }

        System.out.println();

        for(int x = 0; x < 30; x++){
            for(int y = 0; y< 30; y++){
                System.out.print(freeSquares[y][x] + " ");
            }
            System.out.println();
        }

        // Verify that from and to position are on roads
        if(freeSquares[toX][toY] != 1){
            throw new Exception("From or To positions not on road !");
        }

        return this.aStar(objectType, freeSquares, new PositionDto(fromX, fromY), new PositionDto(toX, toY));
    }


    PositionDto[] subway1Positions = new PositionDto[]{
            new PositionDto(9, 8),
            new PositionDto(8, 9),
    };

    PositionDto[] subway2Positions = new PositionDto[]{
            new PositionDto(15, 26),
            new PositionDto(16, 25),
    };

    /**
     * A* implementation, please read https://en.wikipedia.org/wiki/A*_search_algorithm
     */
    private List<PositionDto> aStar(int objectType, int[][] freeSquares, PositionDto from, PositionDto to){
        HashMap<PositionDto, PositionDto> cameFrom = new HashMap<>();

        ArrayList<PositionDto> openSet = new ArrayList<>();
        openSet.add(from);

        // Initialize gScore
        HashMap<PositionDto, Double> gScore = new HashMap<>();
        for(int x = 0; x < freeSquares.length ; x++){
            for(int y = 0; y < freeSquares[0].length ; y++){
                PositionDto position = new PositionDto(x, y);
                gScore.put(position, 999999.0);
            }
        }
        gScore.put(from, 0.0);

        // Initialize fScore
        HashMap<PositionDto, Double> fScore = new HashMap<>();
        for(int x = 0; x < freeSquares.length ; x++){
            for(int y = 0; y < freeSquares[0].length ; y++){
                PositionDto position = new PositionDto(x, y);
                fScore.put(position, 999999.0);
            }
        }
        fScore.put(from, euclidianDistance(from, to));

        while(openSet.size() != 0){

            // Find the node in openSet with lowest fscore
            PositionDto current = null;
            double lowestFScore = Double.MAX_VALUE;
            for(PositionDto openItem: openSet){
                if(fScore.get(openItem) < lowestFScore){
                    lowestFScore = fScore.get(openItem);
                    current = openItem;
                }
            }

            // Arrived at destination !
            if(current.equals(to)){
                System.out.println("END");
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            ArrayList<PositionDto> neighbors = new ArrayList<>();

            // Build the neighbors
            PositionDto top = new PositionDto(current.getX(), current.getY() - 1);
            PositionDto right = new PositionDto(current.getX() + 1, current.getY());
            PositionDto bottom = new PositionDto(current.getX(), current.getY() + 1);
            PositionDto left = new PositionDto(current.getX() - 1, current.getY());

            if(top.getY() >= 0 && freeSquares[top.getX()][top.getY()] == 1){
                neighbors.add(top);
            }

            if(right.getX() < 30 && freeSquares[right.getX()][right.getY()] == 1) {
                neighbors.add(right);
            }

            if(bottom.getY() < 30 && freeSquares[bottom.getX()][bottom.getY()] == 1) {
                neighbors.add(bottom);
            }

            if(left.getX() >= 0 && freeSquares[left.getX()][left.getY()] == 1) {
                neighbors.add(left);
            }

            if(objectType == 0){
                // If person, because cars can't take the subway...
                // If next to subway station, add other subway station as neighbors
                if(Arrays.asList(subway1Positions).contains(current)){
                    neighbors.addAll(Arrays.asList(subway2Positions));
                }
                if(Arrays.asList(subway2Positions).contains(current)){
                    neighbors.addAll(Arrays.asList(subway1Positions));
                }
            }



//            System.out.println(neighbors);

            for(PositionDto neighbor: neighbors){
                double tentative_gScore = gScore.get(current) + 1; // assume d(current, neighbor) always == 1

//                System.out.println(neighbor + " " + tentative_gScore + " " + gScore.get(neighbor));
//                System.out.println(openSet);
                // Go to neighbor if good score
                if(tentative_gScore < gScore.get(neighbor)){
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentative_gScore);
                    fScore.put(neighbor, gScore.get(neighbor) + euclidianDistance(neighbor, to));
                    if(!openSet.contains(neighbor)){
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

    private double euclidianDistance(PositionDto from, PositionDto to){
        return Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2));
    }

    private List<PositionDto> reconstructPath(HashMap<PositionDto, PositionDto> cameFrom, PositionDto current){
        ArrayList<PositionDto> totalPath = new ArrayList<>();
        totalPath.add(current);

        while(cameFrom.containsKey(current)){
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        Collections.reverse(totalPath);
        totalPath.remove(0);
        return totalPath;
    }

}
