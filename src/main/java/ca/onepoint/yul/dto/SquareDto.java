package ca.onepoint.yul.dto;

import lombok.Data;

import java.util.Objects;

@Data
public class SquareDto {

    private Integer value;
    private String image;

    public int getValue(){
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SquareDto squareDto = (SquareDto) o;
        return Objects.equals(value, squareDto.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
