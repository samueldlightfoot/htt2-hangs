package dev.lightfoot.sleuthproxysample.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Data {

    public String content;

    public Data mutate() {
        this.content = content + UUID.randomUUID();
        return this;
    }

}
