package com.patlatarlagna.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoDto {
    private Long id;
    private String photoUrl;
    private Boolean main;
}
