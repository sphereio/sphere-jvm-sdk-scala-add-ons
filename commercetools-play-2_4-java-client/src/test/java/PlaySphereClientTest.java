import io.sphere.sdk.client.*;
import io.sphere.sdk.http.HttpMethod;
import io.sphere.sdk.http.HttpResponse;
import org.junit.Test;
import play.libs.F;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.fest.assertions.Assertions.assertThat;

public class PlaySphereClientTest {

    private static final HttpRequestIntent HTTP_REQUEST_INTENT = HttpRequestIntent.of(HttpMethod.GET, "{\"foo\" : 1}");
    private static final HttpResponse HTTP_RESPONSE = HttpResponse.of(200, "la string");

    @Test
    public void successfulExecution() throws Exception {
        final Function<HttpRequestIntent, HttpResponse> function = intent -> HTTP_RESPONSE;
        final PlayJavaSphereClient client = createClient(function);
        final F.Promise<String> promise = client.execute(new SphereRequest<String>() {
            @Override
            public String deserialize(HttpResponse httpResponse) {
                return new String(httpResponse.getResponseBody(), StandardCharsets.UTF_8);
            }

            @Override
            public HttpRequestIntent httpRequestIntent() {
                return HTTP_REQUEST_INTENT;
            }
        });
        assertThat(promise.get(500, TimeUnit.MILLISECONDS)).isEqualTo("la string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ErrorExecution() throws Exception {
        final Function<HttpRequestIntent, HttpResponse> function = intent -> HTTP_RESPONSE;
        final PlayJavaSphereClient client = createClient(function);
        final F.Promise<String> promise = client.execute(new SphereRequest<String>() {
            @Override
            public String deserialize(HttpResponse httpResponse) {
                throw new IllegalArgumentException("boo");
            }

            @Override
            public HttpRequestIntent httpRequestIntent() {
                return HTTP_REQUEST_INTENT;
            }
        });
        promise.get(500, TimeUnit.MILLISECONDS);
    }

    private PlayJavaSphereClient createClient(Function<HttpRequestIntent, HttpResponse> function) {
        return PlayJavaSphereClient.of(TestDoubleSphereClientFactory.createHttpTestDouble(function));
    }
}
