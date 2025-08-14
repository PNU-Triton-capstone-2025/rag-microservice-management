package com.triton.msa.triton_dashboard.private_data.dto;

import java.util.List;

public record PrivateDataUploadResultDto(
    String message,
    List<UploadedFileResultDto> savedFilenames,
    List<UploadedFileResultDto> skippedFilenames
) {}