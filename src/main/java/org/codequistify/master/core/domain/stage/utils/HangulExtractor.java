package org.codequistify.master.core.domain.stage.utils;

import java.util.List;

public class HangulExtractor {
    public final static List<Character> choseongs = List
            .of('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ');
    private final static int JONGSEONGS_COUNT = 28;
    private final static int JUNGSEONGS_COUNT = 21;

    public String extractChoseongs(String src) {
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

        System.out.println(sb);
        return sb.toString();
    }

    public boolean equalsByChoseong(String str, String choseongs) {
        return this.extractChoseongs(str).equals(choseongs);
    }

    public boolean containsByChoseong(String str, String choseongs) {
        if (str.length() > choseongs.length()) {
            return this.extractChoseongs(str).contains(choseongs);
        }
        return choseongs.contains(this.extractChoseongs(str));
    }

    private boolean isHangul(Character ch) {
        int unicode = (int) ch;
        return 0xAC00 <= unicode && unicode <= 0xD7A3; // 한글이면
    }

    private boolean isHangul(int unicode) {
        return 0xAC00 <= unicode && unicode <= 0xD7A3; // 한글이면
    }

    public record ChoCho(
            String choseongs
    ) {

    }
}
