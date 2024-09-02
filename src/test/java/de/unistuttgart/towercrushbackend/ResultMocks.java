/*
package de.unistuttgart.towercrushbackend;

import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.util.StreamUtils.copyToString;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class ResultMocks {

    public static void setupMockBooksResponse(final WireMockServer mockService) throws IOException {
        mockService.stubFor(
            WireMock
                .post(WireMock.urlEqualTo("/internal/submit-game-pass"))
                .willReturn(
                    WireMock
                        .aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            copyToString(
                                ResultMocks.class.getClassLoader()
                                    .getResourceAsStream("payload/get-results-response.json"),
                                defaultCharset()
                            )
                        )
                )
        );
    }
}
*/
