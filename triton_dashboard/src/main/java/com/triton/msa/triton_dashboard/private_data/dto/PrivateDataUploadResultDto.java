package com.triton.msa.triton_dashboard.private_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PrivateDataUploadResultDto(
    String message,
    @JsonProperty("saved_filenames")
    List<UploadedFileResultDto> savedFilenames,
    @JsonProperty("skipped_filenames")
    List<UploadedFileResultDto> skippedFilenames
) {}