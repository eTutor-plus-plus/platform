package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.objects.dispatcher.sql.SQLExerciseDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@Service
public non-sealed class RTClient extends AbstractDispatcherClient{
    final HttpClient client = HttpClient.newHttpClient();
    protected RTClient(ApplicationProperties properties) {
        super(properties);
    }

    public Integer createRTTask(String solution, String maxPoints){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("solution",solution);
        jsonObject.put("maxPoints", maxPoints);
        Integer id = 0;
        URI uri = URI.create("http://localhost:8081/rt/task/addTask");
       HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
            .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            id = Integer.parseInt(response.body().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public Integer editRTTask(String solution, String maxPoints, String id){
        Integer idError = 0;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("solution",solution);
        jsonObject.put("maxPoints", maxPoints);
        jsonObject.put("id", id);
        URI uri = URI.create("http://localhost:8081/rt/task/editTask");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .PUT(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
            .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            idError = Integer.parseInt(response.body().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idError;
    }

    public void deleteRTTask(String id){
        int idDb = Integer.parseInt(id);
        URI uri = URI.create("http://localhost:8081/rt/task/deleteTask/" + idDb);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .DELETE()
            .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
