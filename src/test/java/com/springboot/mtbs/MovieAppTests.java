package com.springboot.mtbs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MovieAppTests {

    @Test
    void appClassIsInstantiable() {
        assertNotNull(new MovieApp());
    }
}
