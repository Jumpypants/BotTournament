package bots;
// This file demonstrates what external library imports would look like
// Note: These imports would fail compilation, but the NetworkLibraryChecker
// can still parse and analyze them from the source code

/*
Example imports that WOULD be flagged as external libraries:

import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

Example imports that WOULD be flagged as network access:

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import io.netty.bootstrap.Bootstrap;
*/

/**
 * This is a demonstration file showing what violations would look like
 */
public class ExampleViolationsDemo {

    public void makeMove() {
        System.out.println("This bot would violate security rules if it had the imports above");
    }
}
