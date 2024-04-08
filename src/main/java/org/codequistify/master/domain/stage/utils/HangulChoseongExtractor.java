package org.codequistify.master.domain.stage.utils;

import java.util.List;

public class HangulChoseongExtractor {
    private final static List<Character> choseongs = List
            .of('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ');
    private final static int JUNGSEONGS_COUNT = 21;
    private final static int JONGSEONGS_COUNT = 28;
    public String extractChoseong(String src) {
        StringBuilder sb = new StringBuilder();

        for (Character ch : src.toCharArray()) {
            int unicode = (int) ch;
            if (!isHangul(unicode)) {
                sb.append(ch);
                continue;
            }
            int code = unicode - 0xAC00;
            int idx = code / (JUNGSEONGS_COUNT * JONGSEONGS_COUNT);
            sb.append(choseongs.get(idx));
        }

        System.out.println(sb.toString());
        return sb.toString();
    }

    private boolean isHangul(Character ch) {
        int unicode = (int) ch;
        return 0xAC00 <= unicode && unicode <= 0xD7A3; // 한글이면
    }

    private boolean isHangul(int unicode) {
        return 0xAC00 <= unicode && unicode <= 0xD7A3; // 한글이면
    }

    public static record ChoCho (
            String choseongs
    ) {

    }
}
