package com.example.eventtourism.service;

@FunctionalInterface
public interface GenericMapper<T, R> {
    R map(T source);
}
