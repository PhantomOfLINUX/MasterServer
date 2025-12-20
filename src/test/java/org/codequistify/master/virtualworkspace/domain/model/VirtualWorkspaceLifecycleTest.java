package org.codequistify.master.virtualworkspace.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualWorkspaceLifecycleTest {

    @Test
    void creating인_경우에는_active여야한다() {
        assertTrue(VirtualWorkspaceLifecycle.creating().isActive());
    }

    @Test
    void running인_경우에는_active여야한다() {
        assertTrue(VirtualWorkspaceLifecycle.running().isActive());
    }

    @Test
    void idle인_경우에는_active여야한다() {
        assertTrue(VirtualWorkspaceLifecycle.idle().isActive());
    }

    @Test
    void terminating인_경우에는_active가_아니어야한다() {
        assertFalse(VirtualWorkspaceLifecycle.terminating().isActive());
    }

    @Test
    void removed인_경우에는_active가_아니어야한다() {
        assertFalse(VirtualWorkspaceLifecycle.removed().isActive());
    }
}

