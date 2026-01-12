package com.example.ticketing.curation.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exhibition")
@DiscriminatorValue("EXHIBITION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibition extends Curation {
    // Exhibition 고유 필드 추가 가능
}
