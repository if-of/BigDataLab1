package ua.karazina.donets.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Data {

    private final List<String> hamMessages;

    private final List<String> spamMessages;
}
