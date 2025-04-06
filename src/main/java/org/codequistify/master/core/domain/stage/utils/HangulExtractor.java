package org.codequistify.master.core.domain.stage.utils;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HangulExtractor {

    // 초성 리스트 (초성 인덱스 기준 순서 중요)
    public static final List<Character> CHOSEONGS = List.of(
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
            'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
            'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    );
    private static final int HANGUL_BASE_CODE = 0xAC00;
    private static final int HANGUL_LAST_CODE = 0xD7A3;
    private static final int JONGSEONGS_COUNT = 28; // 종성 개수
    private static final int JUNGSEONGS_COUNT = 21; // 중성 개수

    /**
     * 문자열에서 초성만 추출합니다.
     */
    public String extractChoseongs(String src) {
        return src.chars()
                  .mapToObj(this::toChoseong)
                  .map(String::valueOf)
                  .collect(Collectors.joining());
    }

    /**
     * 초성 비교 - 완전 일치
     */
    public boolean equalsByChoseong(String str, String choseongs) {
        return extractChoseongs(str).equals(choseongs);
    }

    /**
     * 초성 포함 여부 비교
     */
    public boolean containsByChoseong(String str, String choseongs) {
        String target = extractChoseongs(str);
        return str.length() > choseongs.length()
                ? target.contains(choseongs)
                : choseongs.contains(target);
    }

    /**
     * 한 글자의 초성 문자 반환
     */
    private char toChoseong(int unicode) {
        if (!isHangul(unicode)) {
            return (char) unicode;
        }
        int offset = unicode - HANGUL_BASE_CODE;
        int index = offset / (JUNGSEONGS_COUNT * JONGSEONGS_COUNT);
        return CHOSEONGS.get(index);
    }

    /**
     * 유니코드 값이 한글인지 여부
     */
    private boolean isHangul(int unicode) {
        return HANGUL_BASE_CODE <= unicode && unicode <= HANGUL_LAST_CODE;
    }

    /**
     * 초성 문자열 래퍼 DTO
     */
    public record ChoCho(String choseongs) {
    }
}
