package org.choon.careerbee.util;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NicknameGenerator {
    private static final String[] ADJECTIVES = {
        "귀여운", "멋진", "작은", "큰", "예쁜", "빠른", "느린", "똑똑", "하얀", "검정",
        "신비한", "강력한", "상냥한", "화려한", "차가운", "따뜻한", "용감한", "친절한", "행복한", "슬픈"
    };

    private static final String[] NOUNS = {
        "고양이", "강아지", "사자", "호랑", "여우", "곰", "토끼", "다람", "펭귄", "늑대",
        "부엉이", "기린", "코끼리", "너구리", "치타", "낙타", "물소", "하마", "앵무새", "돌고래"
    };

    private static final Random RANDOM = new Random();

    public static String generate() {
        List<String> candidates =
            Stream.of(ADJECTIVES)
                .flatMap(adj -> Stream.of(NOUNS).map(noun -> adj + noun))
                .filter(nick -> nick.length() >= 4 && nick.length() <= 5)
                .toList();

        return candidates.get(RANDOM.nextInt(candidates.size()));
    }
}
