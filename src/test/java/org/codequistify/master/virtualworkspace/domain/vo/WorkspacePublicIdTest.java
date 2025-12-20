package org.codequistify.master.virtualworkspace.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkspacePublicIdTest {

    @Test
    void 소문자_하이픈_숫자로_구성된_publicId는_생성되어야한다() {
        assertDoesNotThrow(() -> WorkspacePublicId.from("vw-abc123"));
    }

    @Test
    void issue로_발급한_publicId는_DNS_Label로_유효해야한다() {
        WorkspacePublicId issued = WorkspacePublicId.issue();
        assertDoesNotThrow(() -> WorkspacePublicId.from(issued.value()));
    }

    @Test
    void issue는_길이를_지정할_수_있어야한다() {
        WorkspacePublicId issued = WorkspacePublicId.issue(10);
        assertEquals(10, issued.value().length());
    }

    @Test
    void issue에서_길이가_0이면_발급이_실패해야한다() {
        assertThrows(IllegalArgumentException.class, () -> WorkspacePublicId.issue(0));
    }

    @Test
    void 대문자가_포함되어도_자동으로_소문자로_정규화되어야한다() {
        WorkspacePublicId publicId = WorkspacePublicId.from("VW-AbC123");
        assertEquals("vw-abc123", publicId.value());
    }

    @Test
    void publicId가_빈값인_경우에는_생성이_실패해야한다() {
        assertThrows(IllegalArgumentException.class, () -> WorkspacePublicId.from("   "));
    }

    @Test
    void publicId가_DNS_Label_규칙을_위반하면_생성이_실패해야한다() {
        assertThrows(IllegalArgumentException.class, () -> WorkspacePublicId.from("invalid_public_id"));
    }

    @Test
    void publicId가_하이픈으로_시작하면_생성이_실패해야한다() {
        assertThrows(IllegalArgumentException.class, () -> WorkspacePublicId.from("-abc"));
    }
}
