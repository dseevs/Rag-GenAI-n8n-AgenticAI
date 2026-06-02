package com.virtulab.platform.contracts.ai;

import java.util.List;

public record FoodForThoughtResponse(
        List<String> questions,
        String source
) {}
