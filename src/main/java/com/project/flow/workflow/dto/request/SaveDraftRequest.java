package com.project.flow.workflow.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaveDraftRequest(@NotEmpty(message = "Nodes cannot be empty") List<@Valid NodeDto> nodes, List<@Valid EdgeDto> edges) {}
