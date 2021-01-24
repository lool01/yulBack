package ca.onepoint.yul.dto;

import lombok.Data;

@Data
public class AvatarDto {
    private Integer id;
    private String name;
    private Integer type;
    private String image;
    private Integer waiting;
    private Integer main;
    private Integer x;
    private Integer y;

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }
}
