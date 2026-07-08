package com.project.flow.workflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaveDraftRequest(@NotEmpty(message = "Nodes cannot be empty") List<@Valid NodeDto> nodes, List<@Valid EdgeDto> edges) {}
