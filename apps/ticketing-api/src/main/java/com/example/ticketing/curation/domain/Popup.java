package com.example.ticketing.curation.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popup")
@DiscriminatorValue("POPUP")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Popup extends Curation {
    // Popup 고유 필드 추가 가능
}
