package com.esunbank.library;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureTest {

    @Test
    void projectSkeletonIsAvailable() {
        assertThat(LibraryApplication.class).isNotNull();
    }
}
