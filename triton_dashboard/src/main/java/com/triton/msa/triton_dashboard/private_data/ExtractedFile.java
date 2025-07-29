package com.triton.msa.triton_dashboard.private_data;

import java.time.Instant;

public record ExtractedFile(String filename, String content, Instant timestamp) {}

