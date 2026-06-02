package com.virtulab.platform.contracts.events;

import java.util.UUID;

public record EventAcceptedResponse(UUID eventId, boolean duplicate) {}
