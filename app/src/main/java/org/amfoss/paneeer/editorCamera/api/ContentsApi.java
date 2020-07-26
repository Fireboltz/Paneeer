package org.amfoss.paneeer.editorCamera.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ContentsApi {

    @GET("/api/v3/{api_key}")
    Call<ContentsResponse> getContents(@Path("api_key") String apiKey);
}
