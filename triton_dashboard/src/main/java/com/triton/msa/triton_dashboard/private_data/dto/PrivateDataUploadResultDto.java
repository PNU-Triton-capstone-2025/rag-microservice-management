package com.triton.msa.triton_dashboard.private_data.dto;

import java.util.List;
import java.util.Map;

public record PrivateDataUploadResultDto(
    String message,
    List<FileResultDto> savedFilenames,
    List<FileResultDto> skippedFilenames
) {}