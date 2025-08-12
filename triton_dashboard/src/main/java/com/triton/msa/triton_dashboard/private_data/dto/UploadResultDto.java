package com.triton.msa.triton_dashboard.private_data.dto;

import java.util.List;

public record UploadResultDto (
    String message,
    List<String> savedFilenames,
    List<String> skippedFilenames
) {}