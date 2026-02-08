package com.example.ticketing.collection.constant;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 팝업 카테고리 상수
 */
public final class PopupCategory {

    public static final String FASHION = "패션";
    public static final String BEAUTY = "뷰티";
    public static final String FNB = "F&B";
    public static final String CHARACTER = "캐릭터";
    public static final String TECH = "테크";
    public static final String LIFESTYLE = "라이프스타일";
    public static final String FURNITURE = "가구&인테리어";

    public static final List<String> ALL_CATEGORIES = List.of(
            FASHION, BEAUTY, FNB, CHARACTER, TECH, LIFESTYLE, FURNITURE
    );

    public static final Set<String> CATEGORY_SET = Set.copyOf(ALL_CATEGORIES);

    private PopupCategory() {}

    /**
     * 주어진 카테고리 목록에서 허용된 카테고리만 필터링
     */
    public static List<String> filterAllowed(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return categories.stream()
                .filter(CATEGORY_SET::contains)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리가 허용된 것인지 확인
     */
    public static boolean isAllowed(String category) {
        return category != null && CATEGORY_SET.contains(category);
    }
}
