package com.example.ticketing.curation.dto;

import java.util.List;

public record UserContext(
    List<String> likedPopupIds
) {
}