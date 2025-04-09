package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GitHubActivity {

  public static void main(String[] args) throws IOException, InterruptedException {
    GitHubActivity github = new GitHubActivity();
    github.fetchActivity("AmbatiVamsidharReddy02");
  }

  public void fetchActivity(String username) throws IOException, InterruptedException {
    String API_URL = "https://api.github.com/users/" + username + "/events";
    HttpClient client = HttpClient.newHttpClient();

    try {
      HttpRequest request = HttpRequest.newBuilder().uri(new URI(API_URL))
          .header("Accept", "application/vnd.github.json").GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//      System.out.println("Response: " + response.body());

      if (response.statusCode() == 404) {
        System.out.println("User not found. Please check username");
        return;
      }

      if (response.statusCode() == 200) {
        JsonParser parser = new JsonParser();
        JsonElement result = parser.parse(response.body());
        JsonArray jsonArray = result.getAsJsonArray();
        displayActivity(jsonArray);
      } else {
        System.out.println("Error:" + response.statusCode());
      }
    } catch (IOException ioException) {
      ioException.printStackTrace();
    } catch (InterruptedException interruptedException) {
      interruptedException.printStackTrace();
    } catch (URISyntaxException uriSyntaxException) {
      uriSyntaxException.printStackTrace();
    }
  }

  /*
  TODO
   handle errors such as invalid usernames, repos and payloads
   */

  public static void displayActivity(JsonArray events) {
    for (JsonElement element : events) {
      JsonObject event = element.getAsJsonObject();
      JsonObject repo = event.getAsJsonObject("repo");
      String type = event.get("type").getAsString();
      String repoName = repo != null && repo.has("name") ? repo.get("name").getAsString() : "Unknown Repo";
      String action = "";

      switch (type) {
        case "PushEvent":
          JsonObject payload = event.getAsJsonObject("payload");
          if (payload != null && payload.has("commits")) {
            int commitCount = payload.getAsJsonArray("commits").size();
            action = "Pushed " + commitCount + " commit(s) to " + repoName;
          }
          break;
        case "IssuesEvent":
          payload = event.getAsJsonObject("payload");
          if (payload != null && payload.has("action")) {
            String issueAction = payload.get("action").getAsString();
            action = issueAction.toUpperCase().charAt(0) + issueAction.substring(1)
                + " an issue in " + repoName;
          }
          break;
        case "WatchEvent":
          action = "Starred " + repoName;
          break;
        case "ForkEvent":
          action = "Forked " + repoName;
          break;
        case "CreateEvent":
          payload = event.getAsJsonObject("payload");
          if (payload != null && payload.has("ref_type") && repo != null) {
            action = "Created " + payload.get("ref_type").getAsString() + " in " + repoName;
          }
          break;
        default:
          action = type.replace("Event", "") + " in " + repoName;
          break;
      }

      System.out.println(action);
    }
  }
}
