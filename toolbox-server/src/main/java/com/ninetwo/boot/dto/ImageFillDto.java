package com.ninetwo.boot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageFillDto {

    private Integer width;

    private Integer height;

    private String suffix;

    private Double quality;
}
