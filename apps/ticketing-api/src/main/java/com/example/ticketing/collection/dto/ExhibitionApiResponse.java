package com.example.ticketing.collection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
public record ExhibitionApiResponse(
        @JsonProperty("header")
        @JacksonXmlProperty(localName = "header")
        Header header,

        @JsonProperty("body")
        @JacksonXmlProperty(localName = "body")
        Body body
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
            @JsonProperty("resultCode")
            @JacksonXmlProperty(localName = "resultCode")
            String resultCode,

            @JsonProperty("resultMsg")
            @JacksonXmlProperty(localName = "resultMsg")
            String resultMsg
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            @JsonProperty("items")
            @JacksonXmlProperty(localName = "items")
            Items items
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
            @JsonProperty("item")
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "item")
            List<Item> item
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            @JsonProperty("title")
            @JacksonXmlProperty(localName = "title")
            String title,
            @JsonProperty("type")
            @JacksonXmlProperty(localName = "type")
            String type,
            @JsonProperty("period")
            @JacksonXmlProperty(localName = "period")
            String period,
            @JsonProperty("eventPeriod")
            @JacksonXmlProperty(localName = "eventPeriod")
            String eventPeriod,
            @JsonProperty("eventSite")
            @JacksonXmlProperty(localName = "eventSite")
            String eventSite,
            @JsonProperty("charge")
            @JacksonXmlProperty(localName = "charge")
            String charge,
            @JsonProperty("contactPoint")
            @JacksonXmlProperty(localName = "contactPoint")
            String contactPoint,
            @JsonProperty("url")
            @JacksonXmlProperty(localName = "url")
            String url,
            @JsonProperty("imageObject")
            @JacksonXmlProperty(localName = "imageObject")
            String imageObject,
            @JsonProperty("description")
            @JacksonXmlProperty(localName = "description")
            String description,
            @JsonProperty("viewCount")
            @JacksonXmlProperty(localName = "viewCount")
            String viewCount
    ) {}
}