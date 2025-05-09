package com.github.konradb8.payment_methods.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;

public class JsonLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> List<T> loadJson(String filename, TypeReference<List<T>> type) throws Exception {
        return mapper.readValue(new File(filename), type);
    }

}
