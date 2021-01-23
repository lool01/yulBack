package ca.onepoint.yul.dto;

import lombok.Data;

import javax.persistence.*;

@Data
public class MapDto {

    private Integer id;
    private String name;
    private SquareDto[][] square;

    public SquareDto[][] getSquares() {
        return this.square;
    }

}
