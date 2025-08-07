package com.triton.msa.triton_dashboard.private_data.exception;

public class ElasticsearchDeleteException extends RuntimeException {
    public ElasticsearchDeleteException(String message) {
        super(message);
    }
}
