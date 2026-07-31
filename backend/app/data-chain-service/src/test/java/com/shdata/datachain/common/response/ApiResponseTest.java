package com.shdata.datachain.common.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("UP");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("UP");
    }
}

